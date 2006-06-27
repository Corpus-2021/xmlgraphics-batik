/*

   Copyright 2000-2003  The Apache Software Foundation 

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

import org.apache.batik.anim.AnimationTarget;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.SVGTypes;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedLength;
import org.w3c.dom.svg.SVGRectElement;

/**
 * This class implements {@link SVGRectElement}.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id$
 */
public class SVGOMRectElement
    extends    SVGGraphicsElement
    implements SVGRectElement {

    /**
     * Creates a new SVGOMRectElement object.
     */
    protected SVGOMRectElement() {
    }

    /**
     * Creates a new SVGOMRectElement object.
     * @param prefix The namespace prefix.
     * @param owner The owner document.
     */
    public SVGOMRectElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }

    /**
     * <b>DOM</b>: Implements {@link Node#getLocalName()}.
     */
    public String getLocalName() {
        return SVG_RECT_TAG;
    }

    /**
     * <b>DOM</b>: Implements {@link SVGRectElement#getX()}.
     */
    public SVGAnimatedLength getX() {
        return getAnimatedLengthAttribute
            (null, SVG_X_ATTRIBUTE, SVG_RECT_X_DEFAULT_VALUE,
             SVGOMAnimatedLength.HORIZONTAL_LENGTH, false);
    } 

    /**
     * <b>DOM</b>: Implements {@link SVGRectElement#getY()}.
     */
    public SVGAnimatedLength getY() {
        return getAnimatedLengthAttribute
            (null, SVG_Y_ATTRIBUTE, SVG_RECT_Y_DEFAULT_VALUE,
             SVGOMAnimatedLength.VERTICAL_LENGTH, false);
    }

    /**
     * <b>DOM</b>: Implements {@link SVGRectElement#getWidth()}.
     */
    public SVGAnimatedLength getWidth() {
        return getAnimatedLengthAttribute
            (null, SVG_WIDTH_ATTRIBUTE, "",
             SVGOMAnimatedLength.HORIZONTAL_LENGTH, true);
    } 

    /**
     * <b>DOM</b>: Implements {@link SVGRectElement#getHeight()}.
     */
    public SVGAnimatedLength getHeight() {
        return getAnimatedLengthAttribute
            (null, SVG_HEIGHT_ATTRIBUTE, "",
             SVGOMAnimatedLength.VERTICAL_LENGTH, true);
    }

    /**
     * <b>DOM</b>: Implements {@link SVGRectElement#getRx()}.
     */
    public SVGAnimatedLength getRx() {
        SVGAnimatedLength result =
            (SVGAnimatedLength)getLiveAttributeValue(null, SVG_RX_ATTRIBUTE);
        if (result == null) {
            result = new AbstractSVGAnimatedLength
                (this, null, SVG_RX_ATTRIBUTE,
                 SVGOMAnimatedLength.HORIZONTAL_LENGTH, true) {
                    protected String getDefaultValue() {
                        Attr attr = getAttributeNodeNS(null, SVG_RY_ATTRIBUTE);
                        if (attr == null) {
                            return "0";
                        }
                        return attr.getValue();
                    }
                };
            putLiveAttributeValue(null, SVG_RX_ATTRIBUTE,
                                  (LiveAttributeValue)result);
        }
        return result;
    } 

    /**
     * <b>DOM</b>: Implements {@link SVGRectElement#getRy()}.
     */
    public SVGAnimatedLength getRy() {
        SVGAnimatedLength result =
            (SVGAnimatedLength)getLiveAttributeValue(null, SVG_RY_ATTRIBUTE);
        if (result == null) {
            result = new AbstractSVGAnimatedLength
                (this, null, SVG_RY_ATTRIBUTE,
                 SVGOMAnimatedLength.HORIZONTAL_LENGTH, true) {
                    protected String getDefaultValue() {
                        Attr attr = getAttributeNodeNS(null, SVG_RX_ATTRIBUTE);
                        if (attr == null) {
                            return "0";
                        }
                        return attr.getValue();
                    }
                };
            putLiveAttributeValue(null, SVG_RY_ATTRIBUTE,
                                  (LiveAttributeValue)result);
        }
        return result;
    } 

    /**
     * Returns a new uninitialized instance of this object's class.
     */
    protected Node newNode() {
        return new SVGOMRectElement();
    }

    // ExtendedTraitAccess ///////////////////////////////////////////////////

    /**
     * Returns whether the given XML attribute is animatable.
     */
    public boolean isAttributeAnimatable(String ns, String ln) {
        if (ns == null) {
            if (ln.equals(SVG_X_ATTRIBUTE)
                    || ln.equals(SVG_Y_ATTRIBUTE)
                    || ln.equals(SVG_WIDTH_ATTRIBUTE)
                    || ln.equals(SVG_HEIGHT_ATTRIBUTE)
                    || ln.equals(SVG_RX_ATTRIBUTE)
                    || ln.equals(SVG_RY_ATTRIBUTE)) {
                return true;
            }
        }
        return super.isAttributeAnimatable(ns, ln);
    }

    /**
     * Returns the type of the given attribute.
     */
    public int getAttributeType(String ns, String ln) {
        if (ns == null) {
            if (ln.equals(SVG_X_ATTRIBUTE)
                    || ln.equals(SVG_Y_ATTRIBUTE)
                    || ln.equals(SVG_RX_ATTRIBUTE)
                    || ln.equals(SVG_RY_ATTRIBUTE)
                    || ln.equals(SVG_WIDTH_ATTRIBUTE)
                    || ln.equals(SVG_HEIGHT_ATTRIBUTE)) {
                return SVGTypes.TYPE_LENGTH;
            }
        }
        return super.getAttributeType(ns, ln);
    }

    // AnimationTarget ///////////////////////////////////////////////////////

    /**
     * Gets how percentage values are interpreted by the given attribute.
     */
    protected short getAttributePercentageInterpretation(String ns, String ln) {
        if (ns == null) {
            if (ln.equals(SVG_X_ATTRIBUTE) || ln.equals(SVG_RX_ATTRIBUTE)) {
                return AnimationTarget.PERCENTAGE_VIEWPORT_WIDTH;
            }
            if (ln.equals(SVG_Y_ATTRIBUTE) || ln.equals(SVG_RY_ATTRIBUTE)) {
                return AnimationTarget.PERCENTAGE_VIEWPORT_HEIGHT;
            }
        }
        return super.getAttributePercentageInterpretation(ns, ln);
    }

    /**
     * Updates an attribute value in this target.
     */
    public void updateAttributeValue(String ns, String ln,
                                     AnimatableValue val) {
        if (ns == null) {
            if (ln.equals(SVG_X_ATTRIBUTE)) {
                updateLengthAttributeValue(getX(), val);
                return;
            } else if (ln.equals(SVG_Y_ATTRIBUTE)) {
                updateLengthAttributeValue(getY(), val);
                return;
            } else if (ln.equals(SVG_RX_ATTRIBUTE)) {
                updateLengthAttributeValue(getRx(), val);
                return;
            } else if (ln.equals(SVG_RY_ATTRIBUTE)) {
                updateLengthAttributeValue(getRy(), val);
                return;
            } else if (ln.equals(SVG_WIDTH_ATTRIBUTE)) {
                updateLengthAttributeValue(getWidth(), val);
                return;
            } else if (ln.equals(SVG_HEIGHT_ATTRIBUTE)) {
                updateLengthAttributeValue(getHeight(), val);
                return;
            }
        }
        super.updateAttributeValue(ns, ln, val);
    }
}
