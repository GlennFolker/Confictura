package confictura.cinematic;

import java.io.*;

public class Cinematic{
    public void clear(){

    }

    public void write(DataOutput stream) throws IOException{
        stream.writeShort(0);
    }

    public void read(DataInput stream) throws IOException{
        stream.readShort();
    }
}
