import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        RingBuffer<String> buffer = new RingBuffer<>(3);

        BufferReader<String> fastReader = buffer.createReader();
        BufferReader<String> slowReader = buffer.createReader();

        // Writer writes 2 items (['A', 'B'])
        buffer.write("A");
        buffer.write("B");

        System.out.println("Fast Reader: " + fastReader.read().orElse("Empty")); // A
        System.out.println("Fast Reader: " + fastReader.read().orElse("Empty")); // B

        // Writer writes 3 more items (['D', 'E', 'C'])
        buffer.write("C");
        buffer.write("D");
        buffer.write("E");

        System.out.println("Fast Reader: " + fastReader.read().orElse("Empty")); // C
        System.out.println("Fast Reader: " + fastReader.read().orElse("Empty")); // D

        // Slow reader hasn't read anything yet. It missed A and B
        // It will automatically catch up to the oldest available data (C).
        System.out.println("\n--- Slow Reader catching up ---");
        Optional<String> slowData = slowReader.read();
        System.out.println("Slow Reader: " + slowData.orElse("Empty")); // C
    }
}