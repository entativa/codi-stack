package io.codibase.server.mail;

class ImageAttachment extends Attachment {

	public ImageAttachment(String url, String fileName) {
		super(url, fileName);
	}

	@Override
	public String getMarkdown() {
		return String.format("![%s](%s)", fileName, url);
	}

}
