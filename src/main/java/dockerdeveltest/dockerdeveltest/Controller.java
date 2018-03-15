package dockerdeveltest.dockerdeveltest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxSessionStore;
import com.dropbox.core.DbxStandardSessionStore;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.DbxWebAuth.Request.Builder;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.ThumbnailErrorException;

@RestController
public class Controller {

	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();

	final String APP_KEY = "jr0tecrty7appm4";
	final String APP_SECRET = "ylhfjprpfvwec5s";
	DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
	DbxRequestConfig config = new DbxRequestConfig("JavaTutorial/1.0");
	DbxSessionStore csrfTokenStore;
	DbxWebAuth webAuth;
	String redirectUri = "/graph.html";
	String accessToken;

	@RequestMapping("/greeting")
	public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}

	@RequestMapping("/authenticate")
	public String greeting(HttpServletRequest request, HttpServletResponse response) throws IOException {

		// Create the request objects
		csrfTokenStore = new DbxStandardSessionStore(request.getSession(true), "dropbox-auth-csrf-token");
		webAuth = new DbxWebAuth(config, appInfo);
		Builder builder = DbxWebAuth.newRequestBuilder();

		// Get the dropbox authorization URL
		builder.withRedirectUri(request.getScheme() + "://" + request.getServerName() + redirectUri, csrfTokenStore);
		String authorizeUrl = webAuth.authorize(builder.build());

		response.sendRedirect(authorizeUrl);
		return "redirect:" + authorizeUrl;
	}

	@RequestMapping("/filecontent")
	public String filecontent(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("filename") String filename)
			throws IOException, ListFolderErrorException, DbxException, JSONException {

		JSONObject json = new JSONObject();
		json.put("filename", filename);

		return json.toString();
	}

	@RequestMapping("/filelist")
	public FileList filelist(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("code") String code) throws IOException, ListFolderErrorException, DbxException {

		DbxAuthFinish authFinish = null;

		if (accessToken == null) {

			try {
				authFinish = webAuth.finishFromRedirect(
						request.getScheme() + "://" + request.getServerName() + redirectUri, csrfTokenStore,
						request.getParameterMap());
			} catch (

			DbxWebAuth.BadRequestException ex) {
				response.sendError(400);
			} catch (DbxWebAuth.BadStateException ex) {
				response.sendRedirect("http://my-server.com/dropbox-auth-start");
			} catch (DbxWebAuth.CsrfException ex) {
				response.sendError(403, "Forbidden.");
			} catch (DbxWebAuth.NotApprovedException ex) {
				response.sendError(503, "Not approved exception.");
			} catch (DbxWebAuth.ProviderException ex) {
				response.sendError(503, "Error communicating with Dropbox.");
			} catch (DbxException ex) {
				response.sendError(503, "Error communicating with Dropbox.");
			}

			accessToken = authFinish.getAccessToken();
		}

		// Get all files in the specified folder
		DbxClientV2 client = new DbxClientV2(config, accessToken);
		ListFolderResult listing = client.files().listFolderBuilder("/Life Log").start();

		List<Map<String, String>> fileList = new ArrayList<Map<String, String>>();
		for (Metadata item : listing.getEntries()) {

			String filename = FilenameUtils.removeExtension(item.getName());
			String extension = FilenameUtils.getExtension(item.getName());

			if (extension.equals("gpx")) {
				String title = filename.split("\\xA7")[2];

				String dateWithDashes = getDateWithDashes(item);
				String dateWithSlashes = getDateWithSlashes(item);

				Map<String, String> map = new HashMap<String, String>();
				map.put("dateDash", dateWithDashes);
				map.put("dateSlash", dateWithSlashes);
				map.put("title", title);
				map.put("filename", filename);
				map.put("extension", extension);
				fileList.add(map);
			} else if (extension.equals("jpg")) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				getThumbnail(item.getPathLower(), bos, client);
				System.out.println(bos.toString());

			} else {
				// Get coodinates out of the gpx file
			}
		}

		// return the template to display;
		return new FileList(fileList);
	}

	// Date e.g. 2000-01-12
	private String getDateWithDashes(Metadata item) {
		String date = item.getName().substring(0, 8);
		String date2 = date.substring(0, 4) + "-";
		date2 += date.substring(4, 6) + "-";
		date2 += date.substring(6, 8);
		return date2;
	}

	// Date e.g. 12/01/2000
	private String getDateWithSlashes(Metadata item) {
		String date = item.getName().substring(0, 8);
		String date2 = date.substring(6, 8) + "/";
		date2 += date.substring(4, 6) + "/";
		date2 += date.substring(0, 4);
		return date2;
	}

	private void getThumbnail(String path, OutputStream os, DbxClientV2 client)
			throws ThumbnailErrorException, DbxException, IOException {
		DbxDownloader<FileMetadata> listing = client.files().getThumbnail(path);
		listing.download(os);
	}

}