/*

   Copyright 2000-2004,2006  The Apache Software Foundation 

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

import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.util.SVGTypes;

import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedLength;
import org.w3c.dom.svg.SVGAnimatedPreserveAspectRatio;
import org.w3c.dom.svg.SVGImageElement;

/**
 * This class implements {@link SVGImageElement}.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id$
 */
public class SVGOMImageElement
    extends    SVGURIReferenceGraphicsElement
    implements SVGImageElement {

    /**
     * The attribute initializer.
     */
    protected final static AttributeInitializer attributeInitializer;
    static {
        attributeInitializer = new AttributeInitializer(5);
        attributeInitializer.addAttribute(null, null,
                                          SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE,
                                          "xMidYMid meet");
        attributeInitializer.addAttribute(XMLSupport.XMLNS_NAMESPACE_URI,
                                          null, "xmlns:xlink",
                                          XLinkSupport.XLINK_NAMESPACE_URI);
        attributeInitializer.addAttribute(XLinkSupport.XLINK_NAMESPACE_URI,
                                          "xlink", "type", "simple");
        attributeInitializer.addAttribute(XLinkSupport.XLINK_NAMESPACE_URI,
                                          "xlink", "show", "embed");
        attributeInitializer.addAttribute(XLinkSupport.XLINK_NAMESPACE_URI,
                                          "xlink", "actuate", "onLoad");
    }

    /**
     * Creates a new SVGOMImageElement object.
     */
    protected SVGOMImageElement() {
    }

    /**
     * Creates a new SVGOMImageElement object.
     * @param prefix The namespace prefix.
     * @param owner The owner document.
     */
    public SVGOMImageElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }

    /**
     * <b>DOM</b>: Implements {@link Node#getLocalName()}.
     */
    public String getLocalName() {
        return SVG_IMAGE_TAG;
    }

    /**
     * <b>DOM</b>: Implements {@link SVGImageElement#getX()}.
     */
    public SVGAnimatedLength getX() {
        return getAnimatedLengthAttribute
            (null, SVG_X_ATTRIBUTE, SVG_IMAGE_X_DEFAULT_VALUE,
             SVGOMAnimatedLength.HORIZONTAL_LENGTH, false);
    }

    /**
     * <b>DOM</b>: Implements {@link SVGImageElement#getY()}.
     */
    public SVGAnimatedLength getY() {
        return getAnimatedLengthAttribute
            (null, SVG_Y_ATTRIBUTE, SVG_IMAGE_Y_DEFAULT_VALUE,
             SVGOMAnimatedLength.VERTICAL_LENGTH, false);
    }

    /**
     * <b>DOM</b>: Implements {@link SVGImageElement#getWidth()}.
     */
    public SVGAnimatedLength getWidth() {
        return getAnimatedLengthAttribute
            (null, SVG_WIDTH_ATTRIBUTE, "",
             SVGOMAnimatedLength.HORIZONTAL_LENGTH, true);
    }

    /**
     * <b>DOM</b>: Implements {@link SVGImageElement#getHeight()}.
     */
    public SVGAnimatedLength getHeight() {
        return getAnimatedLengthAttribute
            (null, SVG_HEIGHT_ATTRIBUTE, "",
             SVGOMAnimatedLength.VERTICAL_LENGTH, true);
    }

    /**
     * <b>DOM</b>: Implements {@link SVGImageElement#getPreserveAspectRatio()}.
     */
    public SVGAnimatedPreserveAspectRatio getPreserveAspectRatio() {
        return SVGPreserveAspectRatioSupport.getPreserveAspectRatio(this);
    }

    /**
     * Returns the AttributeInitializer for this element type.
     * @return null if this element has no attribute with a default value.
     */
    protected AttributeInitializer getAttributeInitializer() {
        return attributeInitializer;
    }

    /**
     * Returns a new uninitialized instance of this object's class.
     */
    protected Node newNode() {
        return new SVGOMImageElement();
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
                    || ln.equals(SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE)) {
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
                    || ln.equals(SVG_WIDTH_ATTRIBUTE)
                    || ln.equals(SVG_HEIGHT_ATTRIBUTE)) {
                return SVGTypes.TYPE_LENGTH;
            } else if (ln.equals(SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE)) {
                return SVGTypes.TYPE_PRESERVE_ASPECT_RATIO_VALUE;
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
            if (ln.equals(SVG_X_ATTRIBUTE) || ln.equals(SVG_WIDTH_ATTRIBUTE)) {
                return PERCENTAGE_VIEWPORT_WIDTH;
            }
            if (ln.equals(SVG_Y_ATTRIBUTE) || ln.equals(SVG_WIDTH_ATTRIBUTE)) {
                return PERCENTAGE_VIEWPORT_HEIGHT;
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
            if (ln.equals(SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE)) {
                updatePreserveAspectRatioAttributeValue
                    (getPreserveAspectRatio(), val);
                return;
            } else if (ln.equals(SVG_X_ATTRIBUTE)) {
                updateLengthAttributeValue(getX(), val);
                return;
            } else if (ln.equals(SVG_Y_ATTRIBUTE)) {
                updateLengthAttributeValue(getY(), val);
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

    /**
     * Returns the underlying value of an animatable XML attribute.
     */
    public AnimatableValue getUnderlyingValue(String ns, String ln) {
        if (ns == null) {
            if (ln.equals(SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE)) {
                return getBaseValue(getPreserveAspectRatio());
            } else if (ln.equals(SVG_X_ATTRIBUTE)) {
                return getBaseValue
                    (getX(), PERCENTAGE_VIEWPORT_WIDTH);
            } else if (ln.equals(SVG_Y_ATTRIBUTE)) {
                return getBaseValue
                    (getY(), PERCENTAGE_VIEWPORT_HEIGHT);
            } else if (ln.equals(SVG_WIDTH_ATTRIBUTE)) {
                return getBaseValue
                    (getWidth(), PERCENTAGE_VIEWPORT_WIDTH);
            } else if (ln.equals(SVG_HEIGHT_ATTRIBUTE)) {
                return getBaseValue
                    (getHeight(), PERCENTAGE_VIEWPORT_HEIGHT);
            }
        }
        return super.getUnderlyingValue(ns, ln);
    }
}
