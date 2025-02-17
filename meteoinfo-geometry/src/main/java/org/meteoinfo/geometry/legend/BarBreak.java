/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.geometry.legend;

import java.awt.*;

/**
 *
 * @author Yaqiang Wang
 */
public class BarBreak extends PolygonBreak {
    // <editor-fold desc="Variables">
    private Color errorColor;
    private float errorSize;
    private float capSize;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public BarBreak(){
        super();
        errorColor = Color.black;
        errorSize = 1.0f;
        capSize = 0;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get error color
     * @return Error color
     */
    public Color getErrorColor(){
        return this.errorColor;
    }
    
    /**
     * Set error color
     * @param value Error color
     */
    public void setErrorColor(Color value){
        this.errorColor = value;
    }
    
    /**
     * Get error size
     * @return Error size
     */
    public float getErrorSize(){
        return this.errorSize;
    }
    
    /**
     * Set error size
     * @param value Error size
     */
    public void setErrorSize(float value){
        this.errorSize = value;
    }

    /**
     * Get cap size
     * @return Cap size
     */
    public float getCapSize() {
        return this.capSize;
    }

    /**
     * Set cap size
     * @param value Cap size
     */
    public void setCapSize(float value) {
        this.capSize = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
