@Entity
@Table(name="contribution_vector")
public class ContributionVector {
    @Id
    @Column(name="user_id")
    private UUID userId;

    @Column(name="value", nullable=false)
    private double value;

    protected ContributionVector() {}
    public ContributionVector(UUID userId, double value) {
        this.userId = userId; this.value = value;
    }
    // getter/setter
}
