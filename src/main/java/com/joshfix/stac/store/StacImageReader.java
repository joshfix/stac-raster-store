package com.joshfix.stac.store;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.joshfix.stac.utility.StacException;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.DataSourceException;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.parameter.Parameter;
import org.geotools.s3.S3ImageInputStreamImpl;
import org.geotools.s3.geotiff.S3GeoTiffReader;
import org.geotools.util.factory.Hints;
import org.opengis.coverage.grid.Format;
import org.opengis.filter.Filter;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;

public class StacImageReader extends AbstractGridCoverage2DReader {

    private String assetId = "B2";
    protected StacRestClient client;
    protected String[] collections;

    private final Logger LOGGER = Logger.getLogger(StacImageReader.class.getName());

    public StacImageReader(URI uri) throws DataSourceException {
        this(uri, null);
    }

    public StacImageReader(URI uri, Hints uHints) throws DataSourceException {
        super(uri, uHints);

        int port = uri.getPort();
        source = port == 80 || port == -1 ? uri.getScheme() + "://" + uri.getHost() + uri.getPath() :
                uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + uri.getPath();

        List<NameValuePair> params = URLEncodedUtils.parse(uri.toString(), Charset.forName("UTF-8"), '?', '&');
        params.forEach(param -> {
            switch (param.getName()) {
                case KeyNames.COLLECTION:
                    collections = new String[]{param.getValue()};
                    break;
                case KeyNames.ASSET_ID:
                    assetId = param.getValue();
            }
        });

        try {
            client = StacClientFactory.create((String) source);
        } catch (Exception e) {
            LOGGER.severe("Error connecting to STAC at URL " + source.toString());
            throw new DataSourceException("Error connecting to STAC at URL " + source.toString());
        }


        GeoTiffReader sampleReader = getGeoTiffReader(null);
        this.originalEnvelope = sampleReader.getOriginalEnvelope();
        this.originalGridRange = sampleReader.getOriginalGridRange();
        this.crs = sampleReader.getCoordinateReferenceSystem();
    }

    @Override
    public Format getFormat() {
        return new StacImageFormat();
    }

    @Override
    @SuppressWarnings("unchecked")
    public GridCoverage2D read(GeneralParameterValue[] parameters) throws IOException {
        String itemId = null;
        for (GeneralParameterValue parameter : parameters) {
            if (parameter.getDescriptor().equals(StacImageFormat.PARAM_FILTER)) {
                Filter filter = ((Parameter<Filter>) parameter).getValue();
                if (filter instanceof PropertyIsEqualTo) {
                    PropertyIsEqualTo eq = (PropertyIsEqualTo) filter;
                    if (eq.getExpression1() instanceof PropertyName &&
                            ((PropertyName) eq.getExpression1()).getPropertyName().equals("item_id") &&
                            eq.getExpression2() instanceof Literal) {
                        itemId = ((Literal) eq.getExpression2()).getValue().toString();
                    }
                }

            }
        }
        return getGeoTiffReader(itemId).read(parameters);
    }

    @SuppressWarnings("unchecked")
    public GeoTiffReader getGeoTiffReader(String itemId) throws DataSourceException {

        Map item = getItem(itemId);

        if (null == item) {
            throw new DataSourceException("Unable to find item with id '" + itemId + "' in STAC.");
        }

        String imageUrl = null;
        try {
            imageUrl = getAssetImageUrl(item, assetId);
        } catch (Exception e) {
            throw new DataSourceException("Unable to determine the image URL from the STAC item.");
        }

        if (imageUrl == null) {
            throw new IllegalArgumentException("Unable to determine the image URL from the STAC item.");
        }

        int protocolDelimiter = imageUrl.indexOf(":");
        if (protocolDelimiter <= 0) {
            throw new IllegalArgumentException("Unable to determine the protocol STAC item's asset URL: " + imageUrl);
        }

        try {
            // TODO -- this is an ugly, temporary workaround.  i could build the urls for the s3-geotiff reader MUCH better,
            // but I intend to switch to the COG reader as soon as it's released, so it's not worth the extra effort at this point.
            URL url = new URL(imageUrl);
            imageUrl = "s3:/" + url.getPath() + "?useAnon=true&awsRegion=US_WEST_2";
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
        imageUrl = imageUrl.replace("http://", "s3:/");
        imageUrl = imageUrl.replace("https://", "s3:/");
        imageUrl = imageUrl + "?useAnon=true";
*/


        if (true) {
            try {
                return new S3GeoTiffReader(new S3ImageInputStreamImpl(imageUrl));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String protocol = imageUrl.substring(0, protocolDelimiter).toLowerCase();
        try {
            switch (protocol) {
                case "wasb":
                case "wasbs":
                    //return new AzureGeoTiffReader(new AzureImageInputStreamImpl(imageUrl));
                case "s3": {

                    return new S3GeoTiffReader(imageUrl);
                }
                /*
                default: {
                    CogUri cogUri = new CogUri(imageUrl, true);
                    ImageInputStream cogStream = new CogImageInputStreamSpi().createInputStreamInstance(cogUri, true, null);
                    CogImageReadParam param = new CogImageReadParam();
                    param.setRangeReaderClass(HttpRangeReader.class);
                    if (cogStream instanceof CachingCogImageInputStream) {
                        ((CachingCogImageInputStream)cogStream).init(param);
                    } else if (cogStream instanceof DefaultCogImageInputStream) {
                        ((DefaultCogImageInputStream)cogStream).init(param);
                    }
                    CogImageReader reader = new CogImageReader(new StacCogImageReaderSpi());
                    reader.setInput(cogStream);

                    return new StacGeoTiffReader(cogStream);
                }
*/
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return null;
    }

    public Map getItem(String itemId) throws DataSourceException {

        if (itemId == null) {
            return getRandomItem();
        }

        try {
            SearchRequest request = new SearchRequest()
                    .ids(new String[]{itemId})
                    .collections(collections);

            Map<String, Object> itemCollection = client.search(request);
            List<Map> items = (List<Map>) itemCollection.get("features");

            if (!items.isEmpty()) {
                return items.get(0);
            }

            return null;
        } catch (StacException e) {
            throw new DataSourceException(e.getMessage());
        }
    }

    @Override
    public MathTransform getOriginalGridToWorld(String coverageName, PixelInCell pixInCell) {
        try {
            return getGeoTiffReader(null).getOriginalGridToWorld("geotiff_coverage", pixInCell);
        } catch (DataSourceException e) {
            throw new IllegalStateException(e);
        }
    }

    public Map getRandomItem() throws DataSourceException {


        SearchRequest searchRequest = new SearchRequest()
                .limit(1)
                .collections(collections);
        try {
            Map itemCollection = client.search(searchRequest);
            return (Map) ((List) itemCollection.get("features")).get(0);
        } catch (Exception e) {
            LOGGER.severe("Error querying stac with filter ");
            throw new DataSourceException("Error querying stac with filter ", e);
        }
    }

    public static String getAssetImageUrl(Map item, String assetId) {
        if (null == item.get("assets")) {
            return null;
        }

        Map assets = (Map) item.get("assets");
        Map assetMap = (Map)assets.get(assetId);
        return assetMap == null ? null : (String) ((Map)assetMap.get(assetId)).get("href");
    }
}
