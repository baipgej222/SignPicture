package com.kamesuta.mc.signpic.http.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.kamesuta.mc.signpic.http.CommunicateCanceledException;
import com.kamesuta.mc.signpic.http.CommunicateResponse;
import com.kamesuta.mc.signpic.http.ICommunicate;
import com.kamesuta.mc.signpic.state.Progressable;
import com.kamesuta.mc.signpic.state.State;
import com.kamesuta.mc.signpic.util.Downloader;

public class GyazoUpload implements ICommunicate<GyazoUpload.GyazoResult>, Progressable {
	public static final Gson gson = new Gson();

	private final String name;
	private final InputStream upstream;
	private final State state;
	private final String key;
	protected boolean canceled;

	protected GyazoUpload(final String name, final InputStream stream, final State state, final String key) {
		this.name = name;
		this.upstream = stream;
		this.state = state;
		this.key = key;
	}

	public GyazoUpload(final File file, final State state, final String key) throws FileNotFoundException {
		this.name = file.getName();
		this.upstream = new FileInputStream(file);
		state.setName(this.name);
		state.getProgress().setOverall(file.length());
		this.state = state;
		this.key = key;
	}

	@Override
	public State getState() {
		return this.state;
	}

	@Override
	public CommunicateResponse<GyazoResult> communicate() {
		final String url = "https://upload.gyazo.com/api/upload";

		InputStream resstream = null;
		InputStream countupstream = null;
		try {
			// create the post request.
			final HttpPost httppost = new HttpPost(url);
			final MultipartEntityBuilder builder = MultipartEntityBuilder.create();

			countupstream = new CountingInputStream(this.upstream) {
				@Override
				protected void beforeRead(final int n) throws IOException {
					if (GyazoUpload.this.canceled) {
						httppost.abort();
						throw new CommunicateCanceledException();
					}
				}

				@Override
				protected void afterRead(final int n) {
					super.afterRead(n);
					GyazoUpload.this.state.getProgress().done = getByteCount();
				}
			};

			builder.addTextBody("access_token", this.key);
			builder.addBinaryBody("imagedata", countupstream, ContentType.DEFAULT_BINARY, this.name);
			httppost.setEntity(builder.build());

			// execute request
			final HttpResponse response = Downloader.downloader.client.execute(httppost);

			if (response.getStatusLine().getStatusCode()==HttpStatus.SC_OK) {
				final HttpEntity resEntity = response.getEntity();
				if (this.upstream!=null) {
					resstream = resEntity.getContent();
					return new CommunicateResponse<GyazoResult>(gson.<GyazoResult> fromJson(new JsonReader(new InputStreamReader(resstream, Charsets.UTF_8)), GyazoResult.class));
				}
			}
		} catch (final Exception e) {
			return new CommunicateResponse<GyazoResult>(e);
		} finally {
			IOUtils.closeQuietly(countupstream);
			IOUtils.closeQuietly(resstream);
		}
		return new CommunicateResponse<GyazoResult>();
	}

	@Override
	public void cancel() {
		this.canceled = true;
	}

	public static class GyazoResult implements UploadResult {
		public String created_at;
		public String image_id;
		public String permalink_url;
		public String thumb_url;
		public String type;
		public String url;

		@Override
		public String getLink() {
			return this.url;
		}
	}
}
