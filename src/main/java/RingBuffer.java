public class RingBuffer<T> {
    private final T[] buffer;
    private final int capacity;
    private long writeCount = 0;

    @SuppressWarnings("unchecked")
    public RingBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.capacity = capacity;
        this.buffer = (T[]) new Object[capacity];
    }

    public synchronized void write(T item) {
        int index = (int) (writeCount % capacity);
        buffer[index] = item;
        writeCount++;
    }

    public int getCapacity() {
        return capacity;
    }

    public synchronized long getWriteCount() {
        return writeCount;
    }

    protected synchronized T getItemAt(long count) {
        int index = (int) (count % capacity);
        return buffer[index];
    }

    public BufferReader<T> createReader() {
        return new BufferReader<>(this);
    }
}