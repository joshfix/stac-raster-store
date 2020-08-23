package com.joshfix.stac.store;

import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.geotools.util.factory.Hints;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.filter.Filter;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StacImageFormat extends AbstractGridFormat {

    private static final Logger LOGGER = Logger.getLogger(StacImageFormat.class.getName());

    private static final String FILTER = "Filter";

    public static final DefaultParameterDescriptor<Filter>
            PARAM_FILTER = new DefaultParameterDescriptor<>(FILTER, Filter.class, null, null);

    public static final ParameterDescriptor<String> ASSET_ID =
            new DefaultParameterDescriptor(FieldNames.ASSET_ID, String.class, null, "B2");

    public static final ParameterDescriptor<String> COLLECTION =
            new DefaultParameterDescriptor(FieldNames.COLLECTION, String.class, null, "landsat-8-l1");


    public StacImageFormat() {
        writeParameters = null;
        mInfo = new HashMap<>();
        mInfo.put("name", "STAC Raster Store");
        mInfo.put("description","Utilizes asset URLs in STAC items to read GeoTIFF images.");
        mInfo.put("vendor", "Josh Fix");
        mInfo.put("version", "1.0");

        // reading parameters
        readParameters = new ParameterGroup(
                new DefaultParameterDescriptorGroup(
                        mInfo,
                        new GeneralParameterDescriptor[] {
                                READ_GRIDGEOMETRY2D,
                                INPUT_TRANSPARENT_COLOR,
                                SUGGESTED_TILE_SIZE,
                                COLLECTION,
                                PARAM_FILTER}));
    }

    @Override
    public StacImageReader getReader(Object source, Hints hints) {
        //in practice here source is probably almost always going to be a string.
        try {
            URI uri;
            if (source instanceof File) {
                throw new UnsupportedOperationException("Can't instantiate with a File handle");
            }
            else if (source instanceof String) {
                uri = new URI((String)source);
            }
            else if (source instanceof URI) {
                uri = (URI) source;
            }
            else {
                throw new IllegalArgumentException("Can't create ImageInputStream from input of "
                        + "type: " + source.getClass());
            }

            return new StacImageReader(uri, hints);
        }
        catch (Exception e) {
            LOGGER.log(Level.FINE, "Exception raised trying to instantiate image input "
                    + "stream from source.", e);
            throw new RuntimeException(e);
        }

    }



    @Override
    public boolean accepts(Object o, Hints hints) {
        if (o == null) {
            return false;
        }
        //TODO: actually perform some kind of check
        return true;
    }

    @Override
    public boolean accepts(Object source) {
        return this.accepts(source, null);
    }

	@Override
	public AbstractGridCoverage2DReader getReader(Object source) {
		return getReader(source, null);
	}

	@Override
	public GridCoverageWriter getWriter(Object destination) {
		throw new UnsupportedOperationException();
	}

	@Override
	public GeoToolsWriteParams getDefaultImageIOWriteParameters() {
		throw new UnsupportedOperationException();
	}

	@Override
	public GridCoverageWriter getWriter(Object destination, Hints hints) {
		throw new UnsupportedOperationException();
	}
}
