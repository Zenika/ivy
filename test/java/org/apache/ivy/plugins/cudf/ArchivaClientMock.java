package org.apache.ivy.plugins.cudf;

import com.zenika.cudf.model.Binary;
import com.zenika.cudf.model.BinaryId;
import com.zenika.cudf.model.CUDFDescriptor;
import com.zenika.cudf.model.Preamble;
import com.zenika.cudf.model.Request;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Antoine Rouaze <antoine.rouaze@zenika.com>
 */
public class ArchivaClientMock implements ArchivaClient {

    public static final BinaryId BINARY_ID_1 = new BinaryId("jar1", "com.zenika", 0);
    public static final BinaryId BINARY_ID_2 = new BinaryId("jar2", "com.zenika", 0);
    public static final BinaryId BINARY_ID_3 = new BinaryId("jar3", "com.zenika", 0);

    private boolean called = false;

    public ArchivaClientMock() {
    }

    public CUDFDescriptor resolve(CUDFDescriptor descriptor) {
        called = true;
        return createDescriptor();
    }

    public boolean isCalled() {
        return called;
    }

    public CUDFDescriptor createDescriptor() {
        CUDFDescriptor descriptor = new CUDFDescriptor();

        Preamble preamble = createPreamble();
        Set/*<Binary>*/ binaries = createBinaries(BINARY_ID_1, BINARY_ID_2, BINARY_ID_3);
        Request request = createRequest(findBinaryByBinaryId(BINARY_ID_1, binaries));

        descriptor.setPreamble(preamble);
        descriptor.setPackages(binaries);
        descriptor.setRequest(request);

        return descriptor;
    }

    private Preamble createPreamble() {
        Preamble preamble = new Preamble();
        Map/*<String, String>*/ properties = new HashMap();
        properties.put("key", "value");
        preamble.setProperties(properties);
        preamble.setReqChecksum("req");
        preamble.setStatusChecksum("status");
        preamble.setUnivChecksum("univ");
        return preamble;
    }

    private Set/*<Binary>*/ createBinaries(BinaryId binaryId1, BinaryId binaryId2, BinaryId binaryId3) {
        Set/*<Binary>*/ binaries = new LinkedHashSet();
        Binary binary1 = createBinary(binaryId1, "1.0", "jar", false);
        Binary binary2 = createBinary(binaryId2, "1.0.0", "jar", false);
        Binary binary3 = createBinary(binaryId3, "1.2-SNAPSHOT", "jar", true);

        binary1.getDependencies().add(binary2);
        binary1.getDependencies().add(binary3);

        binaries.add(binary1);
        binaries.add(binary2);
        binaries.add(binary3);
        return binaries;
    }

    private Binary createBinary(BinaryId binaryId, String revision, String type, boolean installed) {
        Binary binary = new Binary(binaryId);
        binary.setInstalled(installed);
        binary.setRevision(revision);
        binary.setType(type);
        return binary;
    }

    private Request createRequest(Binary binary1) {
        Request request = new Request();
        request.getInstall().add(binary1);
        return request;
    }

    public Binary findBinaryByBinaryId(BinaryId binaryId, Set/*<Binary>*/ binaries) {
        Iterator iterator = binaries.iterator();
        while (iterator.hasNext()) {
            Binary binary = (Binary) iterator.next();
            if (binary.getBinaryId().equals(binaryId)) {
                return binary;
            }
        }
        return null;
    }
}
