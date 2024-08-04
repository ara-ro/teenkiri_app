package com.beyond.teenkiri.report.service;

import com.beyond.teenkiri.comment.domain.Comment;
import com.beyond.teenkiri.comment.repository.CommentRepository;
import com.beyond.teenkiri.post.domain.Post;
import com.beyond.teenkiri.post.repository.PostRepository;
import com.beyond.teenkiri.qna.domain.QnA;
import com.beyond.teenkiri.qna.repository.QnARepository;
import com.beyond.teenkiri.report.domain.Report;
import com.beyond.teenkiri.report.dto.ReportListResDto;
import com.beyond.teenkiri.report.dto.ReportSaveReqDto;
import com.beyond.teenkiri.report.repository.ReportRepository;
import com.beyond.teenkiri.user_board.domain.user;
import com.beyond.teenkiri.user_board.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
@Transactional(readOnly = true)
public class ReportService {
    private final UserService userService;
    private final ReportRepository reportRepository;
    private final QnARepository qnARepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public ReportService(ReportRepository repository, UserService userService, QnARepository qnARepository, PostRepository postRepository, CommentRepository commentRepository) {
        this.reportRepository = repository;
        this.userService = userService;
        this.qnARepository = qnARepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }


    @Transactional
    public Report reportCreate(ReportSaveReqDto dto){
        user user = userService.findByEmail(dto.getReportEmail());
        QnA qnA = null;
        Post post = null;
        Comment comment = null;

        if (dto.getQnaId() != null) {
            qnA = qnARepository.findById(dto.getQnaId())
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 QnA입니다."));
        } else if (dto.getPostId() != null) {
            post = postRepository.findById(dto.getPostId())
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 Post입니다."));
        }else if (dto.getCommentId() != null){
            comment = commentRepository.findById(dto.getCommentId())
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 Comment입니다."));
        }

        Report report = dto.toEntity(user, qnA, post, comment);
        return reportRepository.save(report);
    }

    public Page<ReportListResDto> reportList(Pageable pageable, String type) {
        Page<Report> reports;
        if ("qna".equals(type)) {
            reports = reportRepository.findByQnaIsNotNull(pageable);
        } else if ("post".equals(type)) {
            reports = reportRepository.findByPostIsNotNull(pageable);
        } else if ("comment".equals(type)) {
            reports = reportRepository.findByCommentIsNotNull(pageable);
        } else {
            reports = reportRepository.findAll(pageable);
        }
        return reports.map(Report::listFromEntity);
    }
}
