package gov.ismonnet.client.resource;

import java.awt.image.BufferedImage;
import java.io.InputStream;

public interface ResourceService {

    InputStream getResourceAsStream(String res);

    BufferedImage getImageResources(String res);
}
