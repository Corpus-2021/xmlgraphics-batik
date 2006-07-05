/*

   Copyright 2003,2006  The Apache Software Foundation 

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.apache.batik.dom.svg;

import org.apache.batik.util.SVGConstants;
import org.w3c.dom.svg.SVGPathSegList;

/**
 * This class provide support for the SVGAnimatedPathData 
 * interface.
 *
 * @author <a href="mailto:nicolas.socheleau@bitflash.com">Nicolas Socheleau</a>
 * @version $Id$
 */
public abstract class SVGAnimatedPathDataSupport {

    /**
     * Default value for the 'd' attribute.
     */
    public static final String D_DEFAULT_VALUE
        = "";

    /**
     * Returns the {@link SVGOMAnimatedPathData} object that implements
     * the {@link SVGAnimatedPathData} for the given element.
     */
    public static SVGOMAnimatedPathData getAnimatedPathData(AbstractElement e) {
        SVGOMAnimatedPathData result = (SVGOMAnimatedPathData)
            e.getLiveAttributeValue(null, SVGConstants.SVG_D_ATTRIBUTE);
        if (result == null) {
            result = new SVGOMAnimatedPathData(e, null,
                                               SVGConstants.SVG_D_ATTRIBUTE,
                                               D_DEFAULT_VALUE);
            SVGOMDocument doc = (SVGOMDocument) e.getOwnerDocument();
            result.addAnimatedAttributeListener
                (doc.getAnimatedAttributeListener());
            e.putLiveAttributeValue(null, SVGConstants.SVG_D_ATTRIBUTE, result);
        }
        return result;
    }

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.svg.SVGAnimatedPathData#getPathSegList()}.
     */
    public static SVGPathSegList getPathSegList(AbstractElement e) {
        return getAnimatedPathData(e).getPathSegList();
    }

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.svg.SVGAnimatedPathData#getNormalizedPathSegList()}.
     */
    public static SVGPathSegList getNormalizedPathSegList(AbstractElement e) {
        return getAnimatedPathData(e).getNormalizedPathSegList();
    }

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.svg.SVGAnimatedPathData#getAnimatedPathSegList()}.
     */
    public static SVGPathSegList getAnimatedPathSegList(AbstractElement e) {
        return getAnimatedPathData(e).getAnimatedPathSegList();
    }

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.svg.SVGAnimatedPathData#getNormalizedPathSegList()}.
     */
    public static SVGPathSegList getAnimatedNormalizedPathSegList
            (AbstractElement e) {
        return getAnimatedPathData(e).getAnimatedNormalizedPathSegList();
    }
}
