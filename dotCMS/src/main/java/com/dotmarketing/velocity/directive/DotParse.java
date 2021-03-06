package com.dotmarketing.velocity.directive;

import java.io.Writer;

import org.apache.velocity.context.Context;

import com.dotmarketing.beans.Host;
import com.dotmarketing.beans.Identifier;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.DotStateException;
import com.dotmarketing.business.PermissionAPI;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.contentlet.model.ContentletVersionInfo;
import com.dotmarketing.portlets.fileassets.business.FileAsset;
import com.liferay.portal.model.User;


public class DotParse extends DotDirective {

  private static final long serialVersionUID = 1L;

  private final String hostIndicator = "//";
  private final String EDIT_ICON =
      "<div class='dot_parseIcon'><a href='javascript:window.top.document.getElementById(\"detailFrame\").contentWindow.editFile(\"${_dotParseInode}\");' title='$_dotParsePath'><span class='editIcon'></span></a></div>";


  @Override
  public final String getName() {

    return "dotParse";
  }






  @Override
  String resolveTemplatePath(final Context context, final Writer writer, final RenderParams params,final String argument) {
    String templatePath = argument;
    boolean live = params.live;
    Host host = params.currentHost;;
    User user = params.user;
    try {

      // if we have a host
      if (templatePath.startsWith(hostIndicator)) {
        templatePath = templatePath.substring(hostIndicator.length(), templatePath.length());
        String hostName = templatePath.substring(0, templatePath.indexOf('/'));
        templatePath = templatePath.substring(templatePath.indexOf('/'), templatePath.length());
        host = APILocator.getHostAPI().findByName(hostName, user, live);
      }

      long lang = params.language.getId();
      Identifier id = APILocator.getIdentifierAPI().find(host, templatePath);


      ContentletVersionInfo cv = APILocator.getVersionableAPI().getContentletVersionInfo(id.getId(), lang);

      if (cv == null) {
        long defualtLang = APILocator.getLanguageAPI().getDefaultLanguage().getId();
        if (defualtLang != lang) {
          cv = APILocator.getVersionableAPI().getContentletVersionInfo(id.getId(), defualtLang);
        }
      }
      String inode = ((live) ? cv.getLiveInode() : cv.getWorkingInode());



      Contentlet c = APILocator.getContentletAPI().find(inode, params.user, params.live);
      FileAsset asset = APILocator.getFileAssetAPI().fromContentlet(c);
      
      
      // add the edit control
      if (!context.containsKey("dontShowIcon") && params.editMode) {
        if (APILocator.getPermissionAPI().doesUserHavePermission(c, PermissionAPI.PERMISSION_READ, user)) {
          String editIcon = new String(EDIT_ICON).replace("${_dotParseInode}", c.getInode()).replace("${_dotParsePath}",
              id.getParentPath());
          writer.append(editIcon);
        }
      }


      return asset.getFileAsset().getAbsolutePath();
    } catch (Exception e) {
      throw new DotStateException(e);
    }
  }


}

