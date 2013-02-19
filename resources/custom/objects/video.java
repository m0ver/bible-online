package custom.objects;

import java.util.Date;

import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.AbstractData;

public class video extends AbstractData {
	private String filmId;
	private String videoServer;
	private String videoDirectory;
	private String videoFile;
	private String videoUrl;
	private String videoScreenshots;
	private int status;
	private Date uploadedTime;

	public String getId()
	{
		return String.valueOf(this.Id);
	}

	public void setFilmId(String filmId)
	{
		this.filmId=this.setFieldAsString("filmId",filmId);
	}

	public String getFilmId()
	{
		return this.filmId;
	}

	public void setVideoServer(String videoServer)
	{
		this.videoServer=this.setFieldAsString("videoServer",videoServer);
	}

	public String getVideoServer()
	{
		return this.videoServer;
	}

	public void setVideoDirectory(String videoDirectory)
	{
		this.videoDirectory=this.setFieldAsString("videoDirectory",videoDirectory);
	}

	public String getVideoDirectory()
	{
		return this.videoDirectory;
	}

	public void setVideoFile(String videoFile)
	{
		this.videoFile=this.setFieldAsString("videoFile",videoFile);
	}

	public String getVideoFile()
	{
		return this.videoFile;
	}

	public void setVideoUrl(String videoUrl)
	{
		this.videoUrl=this.setFieldAsString("videoUrl",videoUrl);
	}

	public String getVideoUrl()
	{
		return this.videoUrl;
	}

	public void setVideoScreenshots(String videoScreenshots)
	{
		this.videoScreenshots=this.setFieldAsString("videoScreenshots",videoScreenshots);
	}

	public String getVideoScreenshots()
	{
		return this.videoScreenshots;
	}

	public void setStatus(int status)
	{
		this.status=this.setFieldAsInt("status",status);
	}

	public int getStatus()
	{
		return this.status;
	}

	public void setUploadedTime(Date uploadedTime)
	{
		this.uploadedTime=this.setFieldAsDate("uploadedTime",uploadedTime);
	}

	public Date getUploadedTime()
	{
		return this.uploadedTime;
	}


	@Override
	public void setData(Row row) {
		if(row.getFieldInfo("id")!=null)	this.setId(row.getFieldInfo("id").stringValue());
		if(row.getFieldInfo("film_id")!=null)	this.setFilmId(row.getFieldInfo("film_id").stringValue());
		if(row.getFieldInfo("video_server")!=null)	this.setVideoServer(row.getFieldInfo("video_server").stringValue());
		if(row.getFieldInfo("video_directory")!=null)	this.setVideoDirectory(row.getFieldInfo("video_directory").stringValue());
		if(row.getFieldInfo("video_file")!=null)	this.setVideoFile(row.getFieldInfo("video_file").stringValue());
		if(row.getFieldInfo("video_url")!=null)	this.setVideoUrl(row.getFieldInfo("video_url").stringValue());
		if(row.getFieldInfo("video_screenshots")!=null)	this.setVideoScreenshots(row.getFieldInfo("video_screenshots").stringValue());
		if(row.getFieldInfo("status")!=null)	this.setStatus(row.getFieldInfo("status").intValue());
		if(row.getFieldInfo("uploaded_time")!=null)	this.setUploadedTime(row.getFieldInfo("uploaded_time").dateValue());
	}

	@Override
	public String toString() {
		StringBuffer buffer=new StringBuffer();
		buffer.append("{");
		buffer.append("\"Id\":\""+this.getId()+"\"");
		buffer.append(",\"filmId\":\""+this.getFilmId()+"\"");
		buffer.append(",\"videoServer\":\""+this.getVideoServer()+"\"");
		buffer.append(",\"videoDirectory\":\""+this.getVideoDirectory()+"\"");
		buffer.append(",\"videoFile\":\""+this.getVideoFile()+"\"");
		buffer.append(",\"videoUrl\":\""+this.getVideoUrl()+"\"");
		buffer.append(",\"videoScreenshots\":\""+this.getVideoScreenshots()+"\"");
		buffer.append(",\"status\":"+this.getStatus());
		buffer.append(",\"uploadedTime\":\""+this.getUploadedTime()+"\"");
		buffer.append("}");
		return buffer.toString();
	}

}