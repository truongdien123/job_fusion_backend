package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.exceptions.NotFoundException;
import com.tma.job_fusion_backend.models.*;
import com.tma.job_fusion_backend.pojo.requests.CvEvaluateRequest;
import com.tma.job_fusion_backend.pojo.responses.CvEvaluateResponse;
import com.tma.job_fusion_backend.repositories.*;
import com.tma.job_fusion_backend.services.CvAiService;
import com.tma.job_fusion_backend.services.CvEvaluationService;
import com.tma.job_fusion_backend.services.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import com.tma.job_fusion_backend.utils.DateTimeUtil;
import java.time.LocalDateTime;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class CvEvaluationServiceImpl implements CvEvaluationService {

    private final CandidateResumeRepository candidateResumeRepository;
    private final CandidateApplicationRepository candidateApplicationRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobCriteriaRepository jobCriteriaRepository;
    private final CvAiService cvAiService;
    private final SkillService skillService;
    private final CandidateResumeSkillRepository candidateResumeSkillRepository;
    private final CvMatchingResultRepository cvMatchingResultRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String SCORE = "score";
    private final String SKILLS = "skills";
    private final String CRITERION_NAME = "criterionName";
    private final String SUGGESTIONS = "suggestions";

    @Override
    @Transactional
    public void clearEvaluationAndMatchingResult(CandidateResume resume, CandidateApplication application) {
        LocalDateTime now = DateTimeUtil.nowUtc();

        // Soft delete CandidateResumeSkill records
        List<CandidateResumeSkill> activeSkills = candidateResumeSkillRepository.findByResumeAndDeletedAtIsNull(resume);
        if (activeSkills != null && !activeSkills.isEmpty()) {
            for (CandidateResumeSkill crs : activeSkills) {
                crs.setDeletedAt(now);
            }
            candidateResumeSkillRepository.saveAll(activeSkills);
        }

        // Soft delete CvMatchingResult records
        cvMatchingResultRepository.findByApplicationAndDeletedAtIsNull(application).ifPresent(matchingResult -> {
            matchingResult.setDeletedAt(now);
            cvMatchingResultRepository.save(matchingResult);
        });
    }

    @Override
    @Transactional
    public void asyncProcessResumeEvaluation(
            UUID resumeId,
            UUID applicationId,
            byte[] fileBytes,
            String originalFilename,
            String contentType,
            UUID jobId
    ) {
        log.info("Starting async resume evaluation for resume ID: {}, application ID: {}, job ID: {}", resumeId, applicationId, jobId);

        CandidateResume resume = candidateResumeRepository.findByIdAndDeletedAtIsNull(resumeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESUME_NOT_FOUND));

        CandidateApplication application = candidateApplicationRepository.findByIdAndDeletedAtIsNull(applicationId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.APPLICATION_NOT_FOUND));

        JobPosting jobPosting = jobPostingRepository.findByIdAndDeletedAtIsNull(jobId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.JOB_POSTING_NOT_FOUND));

        // Fetch job criteria and evaluate CV using AI service
        List<JobCriteria> jobCriteriaList = jobCriteriaRepository.findByJobIdAndDeletedAtIsNull(jobId);
        List<CvEvaluateRequest.JobCriterionInput> criteriaInputs = jobCriteriaList.stream()
                .map(criteria -> CvEvaluateRequest.JobCriterionInput.builder()
                        .criterionName(criteria.getCriterionName())
                        .description(criteria.getDescription())
                        .weight(criteria.getWeight())
                        .category(criteria.getCategory())
                        .build())
                .toList();

        MultipartFile file = new ByteArrayMultipartFile(fileBytes, "file", originalFilename, contentType);

        CvEvaluateResponse cvEvaluateResponse = null;
        try {
            cvEvaluateResponse = cvAiService.evaluateCv(
                    file,
                    criteriaInputs,
                    jobPosting.getTitle(),
                    jobPosting.getDescription(),
                    jobPosting.getRequirements()
            );
        } catch (Exception e) {
            log.error("Failed to evaluate CV with AI service asynchronously for file: {}, error: {}", originalFilename, e.getMessage(), e);
        }

        if (cvEvaluateResponse != null) {
            resume.setParsedData(cvEvaluateResponse.getParsedData());
            resume.setCandidateSelfScore(cvEvaluateResponse.getCandidateSelfScore());
            resume.setCvImprovementSuggestions(cvEvaluateResponse.getCvImprovementSuggestions());
        } else {
            resume.setParsedData(null);
            resume.setCandidateSelfScore(null);
            resume.setCvImprovementSuggestions(null);
        }
        candidateResumeRepository.save(resume);

        saveCandidateResumeSkillsAndMatchingResult(resume, application, cvEvaluateResponse, jobId);
    }

    private void saveCandidateResumeSkillsAndMatchingResult(
            CandidateResume resume,
            CandidateApplication application,
            CvEvaluateResponse cvEvaluateResponse,
            UUID jobId
    ) {
        // Clean up / soft delete old evaluation and matching records
        clearEvaluationAndMatchingResult(resume, application);

        if (ObjectUtils.isEmpty(cvEvaluateResponse)) {
            return;
        }

        // Save skills to CandidateResumeSkill
        JsonNode parsedData = cvEvaluateResponse.getParsedData();
        if (ObjectUtils.isNotEmpty(parsedData) && parsedData.has(SKILLS)) {
            JsonNode skillsNode = parsedData.get(SKILLS);
            if (skillsNode.isArray()) {
                List<String> rawSkills = new ArrayList<>();
                for (JsonNode skillNode : skillsNode) {
                    String sName = skillNode.asString("");
                    sName = sName.trim();
                    if (!sName.isEmpty()) {
                        rawSkills.add(sName);
                    }
                }

                if (!rawSkills.isEmpty()) {
                    List<Skill> resolvedSkills = skillService.getOrCreateSkills(rawSkills);

                    // Map of name -> Skill for fast lookup
                    Map<String, Skill> skillMap = resolvedSkills.stream()
                            .collect(Collectors.toMap(
                                    s -> s.getName().toLowerCase(),
                                    s -> s,
                                    (s1, s2) -> s1
                            ));

                    // Save relationships
                    List<CandidateResumeSkill> resumeSkills = rawSkills.stream()
                            .map(name -> skillMap.get(name.toLowerCase()))
                            .filter(Objects::nonNull)
                            .distinct()
                            .map(skill -> {
                                CandidateResumeSkill crs = new CandidateResumeSkill();
                                crs.setResume(resume);
                                crs.setSkill(skill);
                                return crs;
                            })
                            .collect(Collectors.toList());

                    if (!resumeSkills.isEmpty()) {
                        candidateResumeSkillRepository.saveAll(resumeSkills);
                        log.info("Saved {} skills for resume ID: {}", resumeSkills.size(), resume.getId());
                    }
                }
            }
        }

        // Save CvMatchingResult
        JsonNode cvImprovementSuggestions = cvEvaluateResponse.getCvImprovementSuggestions();
        if (ObjectUtils.isNotEmpty(cvImprovementSuggestions)) {
            JsonNode suggestionsNode = cvImprovementSuggestions.get(SUGGESTIONS);

            double maxScore = 100.0;

            // Calculate Matching Score
            List<JobCriteria> jobCriteriaList = jobCriteriaRepository.findByJobIdAndDeletedAtIsNull(jobId);
            double matchingScore = 0.0;

            if (ObjectUtils.isNotEmpty(jobCriteriaList)) {
                double scoreSum = 0.0;
                for (JobCriteria criterion : jobCriteriaList) {
                    if (ObjectUtils.isNotEmpty(suggestionsNode) && suggestionsNode.isArray()) {
                        for (JsonNode suggestion : suggestionsNode) {
                            String criterionName = suggestion.path(CRITERION_NAME).asString("");
                            if (criterion.getCriterionName().equalsIgnoreCase(criterionName)) {
                                scoreSum += suggestion.has(SCORE) ? suggestion.get(SCORE).asDouble() : 0.0;
                                break;
                            }
                        }
                    }
                }
                matchingScore = scoreSum;
            } else {
                // Fallback: take candidateSelfScore directly if available
                if (cvEvaluateResponse.getCandidateSelfScore() != null) {
                    matchingScore = cvEvaluateResponse.getCandidateSelfScore();
                } else if (ObjectUtils.isNotEmpty(suggestionsNode) && suggestionsNode.isArray()) {
                    double scoreSum = 0.0;
                    for (JsonNode suggestion : suggestionsNode) {
                        double score = (suggestion.has(SCORE) ? suggestion.get(SCORE).asDouble() : 0.0) * 10.0;
                        scoreSum += score;
                    }
                    matchingScore = (scoreSum / (suggestionsNode.size() * maxScore)) * 100.0;
                }
            }

            // Skill Gaps: suggestions where weighted score is less than 50% of the weight
            ArrayNode skillGapsArray = objectMapper.createArrayNode();
            if (suggestionsNode != null && suggestionsNode.isArray()) {
                for (JsonNode suggestion : suggestionsNode) {
                    String criterionName = suggestion.path(CRITERION_NAME).asString("");
                    double weight = 1.0;
                    if (ObjectUtils.isNotEmpty(jobCriteriaList)) {
                        for (JobCriteria criterion : jobCriteriaList) {
                            if (criterion.getCriterionName().equalsIgnoreCase(criterionName)) {
                                weight = criterion.getWeight() != null ? criterion.getWeight() : 1.0;
                                break;
                            }
                        }
                    } else {
                        weight = suggestion.has("weight") ? suggestion.get("weight").asDouble() : 1.0;
                    }

                    double score = suggestion.has(SCORE) ? suggestion.get(SCORE).asDouble() : 0.0;
                    if (score < (weight * 0.5)) {
                        skillGapsArray.add(suggestion);
                    }
                }
            }

            CvMatchingResult matchingResult = new CvMatchingResult();
            matchingResult.setApplication(application);
            matchingResult.setMatchingScore(matchingScore);
            matchingResult.setReasoning(cvImprovementSuggestions);
            matchingResult.setSkillGaps(skillGapsArray);

            cvMatchingResultRepository.save(matchingResult);
            log.info("Saved CV matching result for application ID: {}, score: {}", application.getId(), matchingScore);
        }
    }

    private static class ByteArrayMultipartFile implements MultipartFile {
        private final byte[] content;
        private final String name;
        private final String originalFilename;
        private final String contentType;

        public ByteArrayMultipartFile(byte[] content, String name, String originalFilename, String contentType) {
            this.content = content;
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
        }

        @Override
        public String getName() { return name; }
        @Override
        public String getOriginalFilename() { return originalFilename; }
        @Override
        public String getContentType() { return contentType; }
        @Override
        public boolean isEmpty() { return content == null || content.length == 0; }
        @Override
        public long getSize() { return content.length; }
        @Override
        public byte[] getBytes() throws IOException { return content; }
        @Override
        public InputStream getInputStream() throws IOException { return new ByteArrayInputStream(content); }
        @Override
        public Resource getResource() {
            return new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return originalFilename;
                }
            };
        }
        @Override
        public void transferTo(File dest) throws IllegalStateException {
            throw new UnsupportedOperationException("Not supported");
        }
    }
}
