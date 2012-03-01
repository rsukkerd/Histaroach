package common;

public class Pair<T, S> {
    private final T first;
    private final S second;
    
    public Pair(T first, S second) {
        this.first = first;
        this.second = second;
    }
    
    public T getFirst() {
        return first;
    }
    
    public S getSecond() {
        return second;
    }
}
