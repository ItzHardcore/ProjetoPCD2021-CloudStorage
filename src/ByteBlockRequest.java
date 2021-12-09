import java.io.Serializable;

public class ByteBlockRequest implements Serializable{
    private int startIndex;
    private int length;

    public ByteBlockRequest(int startIndex, int length){
        this.length = length;
        this.startIndex = startIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
