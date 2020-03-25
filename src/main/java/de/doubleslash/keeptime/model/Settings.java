// Copyright 2019 doubleSlash Net Business GmbH
//
// This file is part of KeepTime.
// KeepTime is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.

package de.doubleslash.keeptime.model;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import de.doubleslash.keeptime.model.persistenceconverter.ColorConverter;
import javafx.scene.paint.Color;

/**
 * Object holding settings
 * 
 * @author nmutter
 */
@Entity
@Table(name = "Settings")
public class Settings {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id", updatable = false, nullable = false)
   private long id;

   @Convert(converter = ColorConverter.class, disableConversion = false)
   private Color hoverBackgroundColor;
   @Convert(converter = ColorConverter.class, disableConversion = false)
   private Color hoverFontColor;
   @Convert(converter = ColorConverter.class, disableConversion = false)
   private Color defaultBackgroundColor;
   @Convert(converter = ColorConverter.class, disableConversion = false)
   private Color defaultFontColor;

   @Convert(converter = ColorConverter.class, disableConversion = false)
   private Color taskBarColor;

   private boolean useHotkey;

   private boolean displayProjectsRight;

   private boolean hideProjectsOnMouseExit;

   private boolean remindIfNotesAreEmpty;

   public long getId() {
      return id;
   }

   public Color getHoverBackgroundColor() {
      return hoverBackgroundColor;
   }

   public void setHoverBackgroundColor(final Color hoverBackgroundColor) {
      this.hoverBackgroundColor = hoverBackgroundColor;
   }

   public Color getHoverFontColor() {
      return hoverFontColor;
   }

   public void setHoverFontColor(final Color hoverFontColor) {
      this.hoverFontColor = hoverFontColor;
   }

   public Color getDefaultBackgroundColor() {
      return defaultBackgroundColor;
   }

   public void setDefaultBackgroundColor(final Color defaultBackgroundColor) {
      this.defaultBackgroundColor = defaultBackgroundColor;
   }

   public Color getDefaultFontColor() {
      return defaultFontColor;
   }

   public void setDefaultFontColor(final Color defaultFontColor) {
      this.defaultFontColor = defaultFontColor;
   }

   public Color getTaskBarColor() {
      return taskBarColor;
   }

   public void setTaskBarColor(final Color taskBarColor) {
      this.taskBarColor = taskBarColor;
   }

   public boolean isUseHotkey() {
      return useHotkey;
   }

   public void setUseHotkey(final boolean useHotkey) {
      this.useHotkey = useHotkey;
   }

   public boolean isDisplayProjectsRight() {
      return displayProjectsRight;
   }

   public void setDisplayProjectsRight(final boolean displayProjectsRight) {
      this.displayProjectsRight = displayProjectsRight;
   }

   public boolean isHideProjectsOnMouseExit() {
      return hideProjectsOnMouseExit;
   }

   public void setHideProjectsOnMouseExit(final boolean hideProjectsOnMouseExit) {
      this.hideProjectsOnMouseExit = hideProjectsOnMouseExit;
   }

   public boolean isRemindIfNotesAreEmpty() {
      return remindIfNotesAreEmpty;
   }

   public void setRemindIfNotesAreEmpty(final boolean emptyNoteReminder) {
      this.remindIfNotesAreEmpty = emptyNoteReminder;
   }

}
