package org.apache.ivy.plugins.parser.cudf;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.plugins.repository.Resource;
import org.apache.ivy.util.url.URLHandlerRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Antoine ROUAZE <antoine.rouaze AT zenika.com>
 */
public class CUDFReader {

    private static final String PACKAGE_START_LINE = "package: ";

    private static final String NUMBER_START_LINE = "number: ";

    private static final String URL_START_LINE = "url: ";

    public static final String SEPARATOR = "%3a";

    private static final String TYPE_START_LINE = "type: ";

    private static final String VERSION_START_LINE = "version: ";

    /**
     * TODO: Add retrieve preamble and properties support
     * @param urlDescriptor
     * @param resource
     * @return
     * @throws IOException
     */
    public CUDFData parse(URL urlDescriptor, Resource resource) throws IOException {
        InputStream inputStream = URLHandlerRegistry.getDefault().openStream(urlDescriptor);
        if (inputStream == null) {
            throw new IllegalStateException();
        }
        CUDFData cudfData = new CUDFData();
        BufferedReader reader = null;
        try {
            List packages = new ArrayList();
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String next = reader.readLine();
            String packageLine = null;
            String versionLine = null;
            String urlLine = null;
            String typeLine = null;
            String cudfVersion = null;
            while (true) {
                String line = next;
                for (next = reader.readLine(); next != null && next.length() > 1 && next.charAt(0) == ' ';
                     next = reader.readLine()) {
                    line = line + next.substring(1);
                }

                if (line == null || (line != null && line.length() == 0)) {
                    if (cudfVersion != null && !"0".equals(versionLine.substring(VERSION_START_LINE.length()).trim())) {
                        packages.add(parseToPackage(packageLine, versionLine, urlLine, typeLine, cudfVersion));
                    }
                    packageLine = versionLine = urlLine = typeLine = cudfVersion = null;
                    if (line == null) {
                        break;
                    }
                }

                if (line.startsWith("#") || line.startsWith("preamble:") || line.startsWith("property: ")
                        || line.startsWith("univ-checksum: ") || (line.length() > 0 && line.charAt(0) == ' ')) {
                    continue;
                }

                line = line.trim();
                if (line.startsWith(PACKAGE_START_LINE)) {
                    packageLine = line;
                } else if (line.startsWith(NUMBER_START_LINE)) {
                    versionLine = line;
                } else if (line.startsWith(URL_START_LINE)) {
                    urlLine = line;
                } else if (line.startsWith(TYPE_START_LINE)) {
                    typeLine = line;
                } else if (line.startsWith(VERSION_START_LINE)) {
                    cudfVersion = line;
                }
            }
            CUDFPackageData mainPackageData = (CUDFPackageData) packages.get(0);
            if (mainPackageData != null) {
                addDependencies(packages, mainPackageData);
                cudfData.setMainPackage((CUDFPackageData) packages.get(0));
            }
        } catch (MalformedURLException e) {
            throw e;
        } catch (IOException e) {
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    return null;
                }
            }
        }
        return cudfData;
    }

    private CUDFPackageData addDependencies(List packages, CUDFPackageData mainPackageData) {
        for (int i = 1; i < packages.size(); i++) {
            mainPackageData.addDependencies((CUDFPackageData) packages.get(i));
        }
        return mainPackageData;
    }

    private CUDFPackageData parseToPackage(String packageLine, String versionLine, String urlLine, String typeLine, String cudfVersion)
            throws MalformedURLException {
        if (packageLine == null || versionLine == null) {
            return null;
        }
        CUDFPackageData packageData = new CUDFPackageData();
        packageData.setPackageName(packageLine.substring(PACKAGE_START_LINE.length()).trim());
        packageData.setNumber(versionLine.substring(NUMBER_START_LINE.length()).trim());
        packageData.setVersion(cudfVersion.substring(VERSION_START_LINE.length()).trim());
        packageData.setType(typeLine == null ? "" : typeLine.substring(TYPE_START_LINE.length()).trim());
        packageData.setUrl(urlLine.substring(URL_START_LINE.length()).trim());
        return packageData;
    }

}
