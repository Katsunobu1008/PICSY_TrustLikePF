@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {
    Optional<TransactionLog> findByRequestId(UUID requestId);
}
