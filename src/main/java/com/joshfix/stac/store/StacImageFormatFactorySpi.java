package com.joshfix.stac.store;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFactorySpi;

import java.awt.*;
import java.util.Collections;
import java.util.Map;

public class StacImageFormatFactorySpi implements GridFormatFactorySpi {

    public AbstractGridFormat createFormat() {
        return new StacImageFormat();
    }

    public boolean isAvailable() {
        return true;
    }

    public Map<RenderingHints.Key, ?> getImplementationHints() {
        return Collections.emptyMap();
    }

}
