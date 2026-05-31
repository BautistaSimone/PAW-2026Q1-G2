package ar.edu.itba.paw.services;

public interface PendingNotificationService {

    void enqueueForFollowers(Long sellerUserId, Long productId);

    void processAndSendDigestEmails();
}
