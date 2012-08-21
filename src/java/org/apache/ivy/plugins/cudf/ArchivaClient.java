package org.apache.ivy.plugins.cudf;

import com.zenika.cudf.model.CUDFDescriptor;

/**
 * @author Antoine Rouaze <antoine.rouaze@zenika.com>
 */
public interface ArchivaClient {
    CUDFDescriptor resolve(CUDFDescriptor descriptor);
}
