import java.util.Optional;

public class BufferReader<T> {
    private final RingBuffer<T> ringBuffer;
    private long nextReadCount = 0;

    BufferReader(RingBuffer<T> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public Optional<T> read() {
        long currentWriteCount = ringBuffer.getWriteCount();
        if (nextReadCount == currentWriteCount) {
            return Optional.empty();
        }

        long capacity = ringBuffer.getCapacity();
        if (currentWriteCount - nextReadCount > capacity) {
            System.out.println("Warning: Reader fell behind. Missed " + (currentWriteCount - nextReadCount - capacity) + " items.");
            nextReadCount = currentWriteCount - capacity;
        }

        T item = ringBuffer.getItemAt(nextReadCount);
        nextReadCount++;

        return Optional.of(item);
    }
}