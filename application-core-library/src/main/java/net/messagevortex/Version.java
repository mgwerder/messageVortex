package net.messagevortex;

// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// ************************************************************************************

public class Version {

  private static final int MAJOR = 0; //@major@
  private static final int MINOR = 2; //@minor@
  private static final int REVISION = 0; //@revision@
  private static final String GIT_BUILD = "$Id$";
  private static final String BUILD = GIT_BUILD.substring(5, GIT_BUILD.length() - 2);

  private static final String VERSION_STRING = MAJOR + "." + MINOR + "." + REVISION;
  private static final String BUILDVER = VERSION_STRING + " (" + BUILD + ")";
  private static final String DATE = "$Format: %cI$";

  private Version() {
    super();
  }

  public static String getBuild() {
    return BUILDVER;
  }

  public static String getVersion() {
    return ""+MessageVortex.class.getPackage().getImplementationVersion();
    // return VERSION_STRING;
  }

  public static String getGitCommitDate() {
    return DATE;
  }

}
