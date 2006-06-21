/*

   Copyright 2006  The Apache Software Foundation 

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
package org.apache.batik.anim.values;

import org.apache.batik.anim.AnimationTarget;

import org.w3c.dom.svg.SVGAngle;

/**
 * An SVG angle value in the animation system.
 *
 * @author <a href="mailto:cam%40mcc%2eid%2eau">Cameron McCormack</a>
 * @version $Id$
 */
public class AnimatableAngleValue extends AnimatableNumberValue {

    /**
     * The angle unit.
     */
    protected short unit;

    /**
     * Creates a new, uninitialized AnimatableAngleValue.
     */
    public AnimatableAngleValue(AnimationTarget target) {
        super(target);
    }

    /**
     * Creates a new AnimatableAngleValue.
     */
    public AnimatableAngleValue(AnimationTarget target, float v, short unit) {
        super(target, v);
        this.unit = unit;
    }

    /**
     * Performs interpolation to the given value.
     */
    public AnimatableValue interpolate(AnimatableValue result,
                                       AnimatableValue to,
                                       float interpolation,
                                       AnimatableValue accumulation,
                                       int multiplier) {
        AnimatableAngleValue res;
        if (result == null) {
            res = new AnimatableAngleValue(target);
        } else {
            res = (AnimatableAngleValue) result;
        }

        float v = value;
        short u = unit;
        if (to != null) {
            AnimatableAngleValue toAngle = (AnimatableAngleValue) to;
            if (toAngle.unit != u) {
                v = rad(v, u);
                v += interpolation * (rad(toAngle.value, toAngle.unit) - v);
                u = SVGAngle.SVG_ANGLETYPE_RAD;
            } else {
                v += interpolation * (toAngle.value - v);
            }
        }
        if (accumulation != null) {
            AnimatableAngleValue accAngle = (AnimatableAngleValue) accumulation;
            if (accAngle.unit != u) {
                v += multiplier * rad(accAngle.value, accAngle.unit);
                u = SVGAngle.SVG_ANGLETYPE_RAD;
            } else {
                v += multiplier * accAngle.value;
            }
        }

        res.value = v;
        res.unit = u;
        return res;
    }

    /**
     * Returns the angle unit.
     */
    public short getUnit() {
        return unit;
    }

    /**
     * Converts an angle value to radians.
     */
    protected float rad(float v, short unit) {
        switch (unit) {
            case SVGAngle.SVG_ANGLETYPE_RAD:
                return v;
            case SVGAngle.SVG_ANGLETYPE_GRAD:
                return (float) Math.PI * v / 200;
            default:
                return (float) Math.PI * v / 180;
        }
    }
}
