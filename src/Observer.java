public interface Observer<S, D> {
    void update(S subject, D data);
}
