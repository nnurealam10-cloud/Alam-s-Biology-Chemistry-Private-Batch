package com.example.data.repository

import com.example.data.local.AlamsDatabase
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AlamsRepository(private val database: AlamsDatabase) {

    // --- Profile & Batch Isolation ---
    val studentProfile: Flow<StudentProfileEntity?> = database.profileDao().getProfile()

    suspend fun switchBatch(newBatch: String) {
        database.profileDao().updateBatch(newBatch)
    }

    suspend fun toggleRole(newRole: String) {
        database.profileDao().updateRole(newRole)
    }

    suspend fun togglePseudonym(usePseudonym: Boolean) {
        database.profileDao().updatePseudonym(usePseudonym)
    }

    suspend fun updateProfile(profile: StudentProfileEntity) {
        database.profileDao().insertProfile(profile)
    }

    // --- Notices ---
    fun getNoticesForBatch(batch: String): Flow<List<NoticeEntity>> {
        return database.noticeDao().getNoticesForBatch(batch)
    }

    fun getAllNotices(): Flow<List<NoticeEntity>> {
        return database.noticeDao().getAllNotices()
    }

    suspend fun markNoticeAsRead(noticeId: String) {
        database.noticeDao().markAsRead(noticeId)
    }

    suspend fun createNotice(
        title: String,
        content: String,
        batch: String,
        category: String,
        isPinned: Boolean
    ) {
        val notice = NoticeEntity(
            id = "not-" + UUID.randomUUID().toString().take(8),
            title = title,
            content = content,
            batch = batch,
            category = category,
            publishedAt = System.currentTimeMillis(),
            isPinned = isPinned,
            isRead = false
        )
        database.noticeDao().insertNotice(notice)
    }

    suspend fun deleteNotice(notice: NoticeEntity) {
        database.noticeDao().deleteNotice(notice)
    }

    // --- Video Lessons (@alamssciencelab) ---
    fun getVideosForBatch(batch: String): Flow<List<VideoLessonEntity>> {
        return database.videoLessonDao().getVideosForBatch(batch)
    }

    fun getAllVideos(): Flow<List<VideoLessonEntity>> {
        return database.videoLessonDao().getAllVideos()
    }

    suspend fun toggleVideoBookmark(id: String, isCurrentlyBookmarked: Boolean) {
        database.videoLessonDao().setBookmark(id, !isCurrentlyBookmarked)
    }

    suspend fun updateVideoProgress(id: String, progress: Int) {
        database.videoLessonDao().updateWatchProgress(id, progress)
    }

    suspend fun registerVideoLesson(
        title: String,
        subject: String,
        batch: String,
        chapter: String,
        youtubeVideoId: String,
        durationMinutes: Int
    ) {
        val cleanVideoId = when {
            youtubeVideoId.contains("v=") -> youtubeVideoId.substringAfter("v=").substringBefore("&")
            youtubeVideoId.contains("youtu.be/") -> youtubeVideoId.substringAfter("youtu.be/").substringBefore("?")
            else -> youtubeVideoId.trim()
        }
        val video = VideoLessonEntity(
            id = "vid-" + UUID.randomUUID().toString().take(8),
            title = title,
            subject = subject,
            batch = batch,
            chapter = chapter,
            youtubeVideoId = cleanVideoId,
            youtubeUrl = "https://youtube.com/watch?v=$cleanVideoId",
            durationMinutes = durationMinutes,
            publishDate = "আজকের আপডেট",
            isBookmarked = false,
            watchProgressPercent = 0
        )
        database.videoLessonDao().insertVideo(video)
    }

    // --- Discussion Forum (Problem Solving Box) ---
    fun getDiscussionMessagesForBatch(batch: String): Flow<List<DiscussionMessageEntity>> {
        return database.discussionDao().getMessagesForBatch(batch)
    }

    fun getAllDiscussionMessages(): Flow<List<DiscussionMessageEntity>> {
        return database.discussionDao().getAllMessages()
    }

    fun getReportedMessages(): Flow<List<DiscussionMessageEntity>> {
        return database.discussionDao().getReportedMessages()
    }

    fun getRepliesForMessage(messageId: String): Flow<List<DiscussionReplyEntity>> {
        return database.discussionDao().getRepliesForMessage(messageId)
    }

    suspend fun postQuestion(
        batch: String,
        authorName: String,
        authorRole: String,
        subject: String,
        chapter: String,
        title: String,
        question: String
    ) {
        val message = DiscussionMessageEntity(
            id = "msg-" + UUID.randomUUID().toString().take(8),
            batch = batch,
            authorName = authorName,
            authorRole = authorRole,
            subject = subject,
            chapter = chapter,
            title = title,
            question = question,
            hasImage = false,
            timestamp = System.currentTimeMillis(),
            isResolved = false,
            replyCount = 0
        )
        database.discussionDao().insertMessage(message)
    }

    suspend fun postReply(
        messageId: String,
        authorName: String,
        authorRole: String,
        replyText: String,
        isHelpful: Boolean = false
    ) {
        val reply = DiscussionReplyEntity(
            id = "rep-" + UUID.randomUUID().toString().take(8),
            messageId = messageId,
            authorName = authorName,
            authorRole = authorRole,
            replyText = replyText,
            timestamp = System.currentTimeMillis(),
            isHelpful = isHelpful
        )
        database.discussionDao().insertReply(reply)
        database.discussionDao().incrementReplyCount(messageId)
    }

    suspend fun reportDiscussionMessage(messageId: String, reason: String) {
        database.discussionDao().reportMessage(messageId, reason)
    }

    suspend fun dismissMessageReport(messageId: String) {
        database.discussionDao().dismissReport(messageId)
    }

    suspend fun deleteDiscussionMessage(messageId: String) {
        database.discussionDao().deleteMessage(messageId)
    }

    // --- Exams & Results ---
    fun getExamsForBatch(batch: String): Flow<List<ExamEntity>> {
        return database.examDao().getExamsForBatch(batch)
    }

    fun getAllExams(): Flow<List<ExamEntity>> {
        return database.examDao().getAllExams()
    }

    suspend fun getExamById(examId: String): ExamEntity? {
        return database.examDao().getExamById(examId)
    }

    suspend fun getQuestionsForExam(examId: String): List<QuestionEntity> {
        return database.examDao().getQuestionsForExam(examId)
    }

    fun getLeaderboard(examId: String): Flow<List<ExamAttemptEntity>> {
        return database.examDao().getAttemptsForExam(examId)
    }

    suspend fun getStudentAttempt(examId: String, roll: String): ExamAttemptEntity? {
        return database.examDao().getStudentAttempt(examId, roll)
    }

    suspend fun submitExamAttempt(
        examId: String,
        studentName: String,
        studentRoll: String,
        batch: String,
        score: Float,
        totalMarks: Int,
        correctCount: Int,
        wrongCount: Int,
        unansweredCount: Int,
        timeTakenSeconds: Int,
        answersJson: String
    ): ExamAttemptEntity {
        val attempt = ExamAttemptEntity(
            id = "att-" + UUID.randomUUID().toString().take(8),
            examId = examId,
            studentName = studentName,
            studentRoll = studentRoll,
            batch = batch,
            score = score,
            totalMarks = totalMarks,
            correctCount = correctCount,
            wrongCount = wrongCount,
            unansweredCount = unansweredCount,
            timestamp = System.currentTimeMillis(),
            timeTakenSeconds = timeTakenSeconds,
            selectedAnswersJson = answersJson
        )
        database.examDao().insertAttempt(attempt)
        return attempt
    }

    suspend fun createExamWithQuestions(
        exam: ExamEntity,
        questions: List<QuestionEntity>
    ) {
        database.examDao().insertExam(exam)
        database.examDao().insertQuestions(questions)
    }

    // --- Fees & bKash/Nagad Submissions ---
    fun getInvoicesForStudent(roll: String): Flow<List<FeeInvoiceEntity>> {
        return database.feeDao().getInvoicesForStudent(roll)
    }

    fun getAllInvoices(): Flow<List<FeeInvoiceEntity>> {
        return database.feeDao().getAllInvoices()
    }

    fun getSubmissionsForStudent(roll: String): Flow<List<PaymentSubmissionEntity>> {
        return database.feeDao().getSubmissionsForStudent(roll)
    }

    fun getPendingSubmissions(): Flow<List<PaymentSubmissionEntity>> {
        return database.feeDao().getPendingSubmissions()
    }

    fun getAllSubmissions(): Flow<List<PaymentSubmissionEntity>> {
        return database.feeDao().getAllSubmissions()
    }

    suspend fun submitPayment(
        invoiceId: String,
        studentName: String,
        studentRoll: String,
        batch: String,
        method: String,
        amount: Int,
        trxId: String,
        senderPhone: String
    ) {
        val maskedPhone = if (senderPhone.length >= 7) {
            senderPhone.take(3) + "****" + senderPhone.takeLast(4)
        } else {
            "017****0000"
        }

        val submission = PaymentSubmissionEntity(
            id = "sub-" + UUID.randomUUID().toString().take(8),
            invoiceId = invoiceId,
            studentName = studentName,
            studentRoll = studentRoll,
            batch = batch,
            method = method,
            amount = amount,
            trxId = trxId.trim().uppercase(),
            senderPhoneMasked = maskedPhone,
            submittedAt = System.currentTimeMillis(),
            status = "PENDING",
            adminNote = null,
            receiptNumber = null,
            verifiedAt = null
        )
        database.feeDao().insertSubmission(submission)
        database.feeDao().updateInvoiceStatus(invoiceId, "PENDING_VERIFICATION", null)
    }

    suspend fun verifyPayment(
        submissionId: String,
        invoiceId: String,
        adminNote: String?
    ) {
        val receiptNumber = "ALAM-REC-" + (1000..9999).random().toString()
        val now = System.currentTimeMillis()
        database.feeDao().updateSubmissionStatus(
            id = submissionId,
            status = "VERIFIED",
            note = adminNote ?: "bKash/Nagad লেনদেন সফলভাবে নিশ্চিত করা হয়েছে।",
            receipt = receiptNumber,
            verifiedAt = now
        )
        database.feeDao().updateInvoiceStatus(invoiceId, "PAID", receiptNumber)
    }

    suspend fun rejectPayment(
        submissionId: String,
        invoiceId: String,
        rejectReason: String
    ) {
        database.feeDao().updateSubmissionStatus(
            id = submissionId,
            status = "REJECTED",
            note = rejectReason,
            receipt = null,
            verifiedAt = System.currentTimeMillis()
        )
        database.feeDao().updateInvoiceStatus(invoiceId, "UNPAID", null)
    }
}
