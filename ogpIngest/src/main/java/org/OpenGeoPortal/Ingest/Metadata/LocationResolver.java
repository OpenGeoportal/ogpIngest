package org.OpenGeoPortal.Ingest.Metadata;

import java.util.Set;

import org.OpenGeoPortal.Layer.LocationLink;
import org.w3c.dom.Document;

public interface LocationResolver {
	Set<LocationLink> resolveLocation(Document xmlDocument);
}
