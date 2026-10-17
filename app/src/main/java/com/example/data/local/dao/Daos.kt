package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoticeDao {
    @Query("SELECT * FROM notices ORDER BY isPinned DESC, publishedAt DESC")
    fun getAllNotices(): Flow<List<NoticeEntity>>

    @Query("SELECT * FROM notices WHERE batch = :batch OR batch = 'ALL' ORDER BY isPinned DESC, publishedAt DESC")
    fun getNoticesForBatch(batch: String): Flow<List<NoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotices(notices: List<NoticeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeEntity)

    @Query("UPDATE notices SET isRead = 1 WHERE id = :noticeId")
    suspend fun markAsRead(noticeId: String)

    @Delete
    suspend fun deleteNotice(notice: NoticeEntity)
}

@Dao
interface VideoLessonDao {
    @Query("SELECT * FROM video_lessons ORDER BY publishDate DESC")
    fun getAllVideos(): Flow<List<VideoLessonEntity>>

    @Query("SELECT * FROM video_lessons WHERE batch = :batch OR batch = 'ALL' ORDER BY publishDate DESC")
    fun getVideosForBatch(batch: String): Flow<List<VideoLessonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoLessonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoLessonEntity)

    @Query("UPDATE video_lessons SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun setBookmark(id: String, bookmarked: Boolean)

    @Query("UPDATE video_lessons SET watchProgressPercent = :progress WHERE id = :id")
    suspend fun updateWatchProgress(id: String, progress: Int)
}

@Dao
interface DiscussionDao {
    @Query("SELECT * FROM discussion_messages WHERE batch = :batch ORDER BY timestamp DESC")
    fun getMessagesForBatch(batch: String): Flow<List<DiscussionMessageEntity>>

    @Query("SELECT * FROM discussion_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<DiscussionMessageEntity>>

    @Query("SELECT * FROM discussion_messages WHERE isReported = 1 ORDER BY timestamp DESC")
    fun getReportedMessages(): Flow<List<DiscussionMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: DiscussionMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<DiscussionMessageEntity>)

    @Query("SELECT * FROM discussion_replies WHERE messageId = :messageId ORDER BY timestamp ASC")
    fun getRepliesForMessage(messageId: String): Flow<List<DiscussionReplyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReply(reply: DiscussionReplyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReplies(replies: List<DiscussionReplyEntity>)

    @Query("UPDATE discussion_messages SET replyCount = replyCount + 1 WHERE id = :messageId")
    suspend fun incrementReplyCount(messageId: String)

    @Query("UPDATE discussion_messages SET isReported = 1, reportReason = :reason WHERE id = :messageId")
    suspend fun reportMessage(messageId: String, reason: String)

    @Query("UPDATE discussion_messages SET isReported = 0, reportReason = null WHERE id = :messageId")
    suspend fun dismissReport(messageId: String)

    @Query("DELETE FROM discussion_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams WHERE batch = :batch ORDER BY startTime DESC")
    fun getExamsForBatch(batch: String): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams ORDER BY startTime DESC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE id = :examId LIMIT 1")
    suspend fun getExamById(examId: String): ExamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExams(exams: List<ExamEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity)

    @Query("SELECT * FROM questions WHERE examId = :examId ORDER BY questionNumber ASC")
    suspend fun getQuestionsForExam(examId: String): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("SELECT * FROM exam_attempts WHERE examId = :examId ORDER BY score DESC, timeTakenSeconds ASC")
    fun getAttemptsForExam(examId: String): Flow<List<ExamAttemptEntity>>

    @Query("SELECT * FROM exam_attempts WHERE examId = :examId AND studentRoll = :roll LIMIT 1")
    suspend fun getStudentAttempt(examId: String, roll: String): ExamAttemptEntity?

    @Query("SELECT * FROM exam_attempts WHERE studentRoll = :roll ORDER BY timestamp DESC")
    fun getAttemptsForStudent(roll: String): Flow<List<ExamAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: ExamAttemptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempts(attempts: List<ExamAttemptEntity>)
}

@Dao
interface FeeDao {
    @Query("SELECT * FROM fee_invoices WHERE studentRoll = :roll ORDER BY dueDate DESC")
    fun getInvoicesForStudent(roll: String): Flow<List<FeeInvoiceEntity>>

    @Query("SELECT * FROM fee_invoices ORDER BY dueDate DESC")
    fun getAllInvoices(): Flow<List<FeeInvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoices(invoices: List<FeeInvoiceEntity>)

    @Query("SELECT * FROM payment_submissions WHERE studentRoll = :roll ORDER BY submittedAt DESC")
    fun getSubmissionsForStudent(roll: String): Flow<List<PaymentSubmissionEntity>>

    @Query("SELECT * FROM payment_submissions ORDER BY submittedAt DESC")
    fun getAllSubmissions(): Flow<List<PaymentSubmissionEntity>>

    @Query("SELECT * FROM payment_submissions WHERE status = 'PENDING' ORDER BY submittedAt DESC")
    fun getPendingSubmissions(): Flow<List<PaymentSubmissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: PaymentSubmissionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmissions(submissions: List<PaymentSubmissionEntity>)

    @Query("UPDATE payment_submissions SET status = :status, adminNote = :note, receiptNumber = :receipt, verifiedAt = :verifiedAt WHERE id = :id")
    suspend fun updateSubmissionStatus(id: String, status: String, note: String?, receipt: String?, verifiedAt: Long?)

    @Query("UPDATE fee_invoices SET status = :status, receiptId = :receipt WHERE id = :invoiceId")
    suspend fun updateInvoiceStatus(invoiceId: String, status: String, receipt: String?)
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM student_profile WHERE id = 'current_user' LIMIT 1")
    fun getProfile(): Flow<StudentProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: StudentProfileEntity)

    @Query("UPDATE student_profile SET batch = :batch WHERE id = 'current_user'")
    suspend fun updateBatch(batch: String)

    @Query("UPDATE student_profile SET role = :role WHERE id = 'current_user'")
    suspend fun updateRole(role: String)

    @Query("UPDATE student_profile SET isPseudonymInRank = :usePseudonym WHERE id = 'current_user'")
    suspend fun updatePseudonym(usePseudonym: Boolean)
}
