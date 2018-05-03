package it.iiizio.epubator.infrastructure.providers;

import it.iiizio.epubator.domain.entities.FrontCoverDetails;

public interface ImageProvider {

	byte[] addSelectedCoverImage(String coverImageFilename, FrontCoverDetails coverDetails);

	byte[] addDefaultCover(String[] titleWords, boolean showLogoOnCover, FrontCoverDetails coverDetails);

}
