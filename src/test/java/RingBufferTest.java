import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RingBufferTest {

    private PrintStream originalOut;
    private ByteArrayOutputStream capturedOut;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        capturedOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void constructorRejectsZeroCapacity() {

        // Arrange
        int capacity = 0;

        // Act + Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RingBuffer<String>(capacity)
        );

        assertEquals("Capacity must be greater than 0", exception.getMessage());
    }

    @Test
    void readFromEmptyBufferReturnsEmptyOptional() {

        // Arrange
        RingBuffer<String> buffer = new RingBuffer<>(3);
        BufferReader<String> reader = buffer.createReader();

        // Act
        Optional<String> result = reader.read();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void readerReadsItemsInFifoOrderBeforeOverflow() {

        // Arrange
        RingBuffer<String> buffer = new RingBuffer<>(3);
        BufferReader<String> reader = buffer.createReader();

        buffer.write("A");
        buffer.write("B");
        buffer.write("C");

        // Act
        Optional<String> first = reader.read();
        Optional<String> second = reader.read();
        Optional<String> third = reader.read();

        // Assert
        assertEquals(Optional.of("A"), first);
        assertEquals(Optional.of("B"), second);
        assertEquals(Optional.of("C"), third);
    }

    @Test
    void writeCountIncreasesForEveryWriteEvenAfterOverwrite() {

        // Arrange
        RingBuffer<Integer> buffer = new RingBuffer<>(2);

        // Act
        buffer.write(10);
        buffer.write(20);
        buffer.write(30);
        buffer.write(40);

        // Assert
        assertEquals(4, buffer.getWriteCount());
    }

    @Test
    void slowReaderSkipsOnlyItemsThatWereOverwritten() {

        // Arrange
        RingBuffer<String> buffer = new RingBuffer<>(3);
        BufferReader<String> reader = buffer.createReader();

        buffer.write("A");
        buffer.write("B");
        buffer.write("C");
        buffer.write("D");
        buffer.write("E");

        // Act
        Optional<String> first = reader.read();
        Optional<String> second = reader.read();
        Optional<String> third = reader.read();

        // Assert
        assertEquals(Optional.of("C"), first);
        assertEquals(Optional.of("D"), second);
        assertEquals(Optional.of("E"), third);
    }

    @Test
    void slowReaderPrintsWarningWithNumberOfMissedItems() {

        // Arrange
        RingBuffer<String> buffer = new RingBuffer<>(3);
        BufferReader<String> reader = buffer.createReader();

        buffer.write("A");
        buffer.write("B");
        buffer.write("C");
        buffer.write("D");
        buffer.write("E");

        // Act
        reader.read();

        // Assert
        String output = capturedOut.toString();
        assertTrue(output.contains("Warning: Reader fell behind. Missed 2 items."));
    }

    @Test
    void capacityOneKeepsOnlyNewestItemForSlowReader() {

        // Arrange
        RingBuffer<String> buffer = new RingBuffer<>(1);
        BufferReader<String> reader = buffer.createReader();

        buffer.write("A");
        buffer.write("B");
        buffer.write("C");

        // Act
        Optional<String> result = reader.read();

        // Assert
        assertEquals(Optional.of("C"), result);
    }

    @Test
    void supportsGenericIntegerValues() {

        // Arrange
        RingBuffer<Integer> buffer = new RingBuffer<>(2);
        BufferReader<Integer> reader = buffer.createReader();

        buffer.write(100);
        buffer.write(200);

        // Act
        Optional<Integer> first = reader.read();
        Optional<Integer> second = reader.read();

        // Assert
        assertEquals(Optional.of(100), first);
        assertEquals(Optional.of(200), second);
    }

    @Test
    void nullItemThrowsNullPointerException() {

        // Arrange
        RingBuffer<String> buffer = new RingBuffer<>(2);
        BufferReader<String> reader = buffer.createReader();

        buffer.write(null);

        // Act + Assert
        assertThrows(NullPointerException.class, reader::read);
    }
}