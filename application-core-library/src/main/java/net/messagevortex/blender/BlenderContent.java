package net.messagevortex.blender;

import java.nio.charset.StandardCharsets;
import java.util.Vector;

public class BlenderContent {

  private final Vector<String> attachments = new Vector<>();

  private String text = "";

  public void addAttachment(byte[] attachment) {
    attachments.add(new String(attachment, StandardCharsets.UTF_8));
  }

  public void clearAttachments() {
    attachments.clear();
  }

  public byte[] getAttachment(int i) {
    return attachments.get(i).getBytes(StandardCharsets.UTF_8);
  }

  public int getNumberOfAttachments() {
    return attachments.size();
  }

  /***
   * <p>Set the message text of the blender content.</p>
   * @param newText the text to be set
   * @return the previously set text
   */
  public String setText(String newText) {
    String old = text;
    this.text = newText;
    return old;
  }

  public String getText() {
    return text;
  }

}
