package org.apache.ivy.plugins.cudf;

import com.zenika.cudf.model.CUDFDescriptor;
import com.zenika.cudf.parser.DefaultSerializer;
import com.zenika.cudf.parser.ParsingException;
import org.apache.commons.httpclient.methods.RequestEntity;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * @author Antoine Rouaze <antoine.rouaze@zenika.com>
 */
public class CUDFRequestEntity implements RequestEntity {

    private final CUDFDescriptor descriptor;

    public CUDFRequestEntity(CUDFDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public boolean isRepeatable() {
        return false;
    }

    public void writeRequest(OutputStream outputStream) throws IOException {
        DefaultSerializer serializer = new DefaultSerializer(new OutputStreamWriter(outputStream));
        try {
            serializer.serialize(descriptor);
        } catch (ParsingException e) {
            throw new RuntimeException("Unable to parse CUDF document", e);
        }
    }

    public long getContentLength() {
        //TODO: Find a way to compute the CUDF document size
        return 0;
    }

    public String getContentType() {
        return "text/plain";
    }
}
