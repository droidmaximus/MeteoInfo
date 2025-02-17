package org.meteoinfo.chart.plot;

import org.meteoinfo.common.*;
import org.meteoinfo.geometry.geoprocess.GeoComputation;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.graphic.GraphicCollection;
import org.meteoinfo.geometry.shape.PointZ;
import org.meteoinfo.geometry.shape.Polyline;
import org.meteoinfo.geometry.shape.PolylineShape;
import org.meteoinfo.geometry.shape.PolylineZShape;
import org.meteoinfo.projection.ProjectionInfo;
import org.meteoinfo.projection.ProjectionNames;
import org.meteoinfo.projection.ProjectionUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MapGridLine extends GridLine {
    protected ProjectionInfo projInfo;
    protected ProjectionInfo longLat = ProjectionInfo.LONG_LAT;
    protected Extent extent;
    protected Extent lonLatExtent;
    protected List<Double> longitudeLocations;
    protected List<Double> latitudeLocations;
    protected boolean fixLocations = false;
    protected GraphicCollection longitudeLines;
    protected GraphicCollection latitudeLines;
    protected int nPoints = 100;
    protected List<GridLabel> gridLabels = new ArrayList<>();

    /**
     * Constructor
     */
    public MapGridLine() {
        this(null, null);
    }

    /**
     * Constructor
     * @param projInfo Projection
     * @param extent Extent
     */
    public MapGridLine(ProjectionInfo projInfo, Extent extent) {
        super(true);
        this.projInfo = projInfo;
        this.setExtent(extent);
    }

    /**
     * Set projection
     * @param projInfo Projection
     */
    public void setProjInfo(ProjectionInfo projInfo) {
        this.projInfo = projInfo;
        updateLonLatExtent();
    }

    /**
     * Set extent
     * @param extent Extent
     */
    public void setExtent(Extent extent) {
        if (extent == null)
            this.extent = extent;
        else
            this.extent = (Extent) extent.clone();
        updateLonLatExtent();
    }

    /**
     * Update longitude/latitude extent
     */
    public void updateLonLatExtent() {
        if (this.projInfo == null || this.extent == null)
            return;

        this.lonLatExtent = ProjectionUtil.getProjectionExtent(this.projInfo, ProjectionInfo.LONG_LAT, extent, 100);
        if (!this.fixLocations) {
            this.longitudeLocations = Arrays.stream(MIMath.getIntervalValues(lonLatExtent.minX, lonLatExtent.maxX)).
                    boxed().collect(Collectors.toList());
            this.updateLongitudeLines();
            this.latitudeLocations = Arrays.stream(MIMath.getIntervalValues(lonLatExtent.minY, lonLatExtent.maxY)).
                    boxed().collect(Collectors.toList());
            this.updateLatitudeLines();
            this.updateLonLatGridLabels();
        }
    }

    /**
     * Set longitude locations
     * @param value Longitude locations
     */
    public void setLongitudeLocations(List<Double> value) {
        this.longitudeLocations = value;
        updateLongitudeLines();
        this.fixLocations = true;
    }

    /**
     * Set latitude locations
     * @param value Latitude locations
     */
    public void setLatitudeLocations(List<Double> value) {
        this.latitudeLocations = value;
        updateLatitudeLines();
        this.fixLocations = true;
    }

    /**
     * Get longitude lines
     * @return Longitude lines
     */
    public GraphicCollection getLongitudeLines() {
        return this.longitudeLines;
    }

    /**
     * Get latitude lines
     * @return Latitude lines
     */
    public GraphicCollection getLatitudeLines() {
        return this.latitudeLines;
    }

    /**
     * Get longitude/latitude grid labels
     * @return Grid labels
     */
    public List<GridLabel> getGridLabels() {
        return this.gridLabels;
    }

    protected void updateLongitudeLines() {
        this.longitudeLines = new GraphicCollection();
        double latMin = this.lonLatExtent.minY;
        double latMax = this.lonLatExtent.maxY;
        double delta = this.lonLatExtent.getHeight() / (this.nPoints - 1);
        for (double lon : this.longitudeLocations) {
            List<PointD> points = new ArrayList<>();
            double lat = latMin;
            while (lat <= latMax) {
                points.add(new PointD(lon, lat));
                lat += delta;
            }
            PolylineShape line = new PolylineShape();
            line.setPoints(points);
            Graphic graphic = new Graphic(line, this.lineBreak);
            graphic = ProjectionUtil.projectClipGraphic(graphic, longLat, projInfo);
            graphic.getShape().setValue(lon);
            this.longitudeLines.add(graphic);
        }
    }

    protected void updateLatitudeLines() {
        this.latitudeLines = new GraphicCollection();
        double lonMin = this.lonLatExtent.minX;
        double lonMax = this.lonLatExtent.maxX;
        if (lonMin < - 170) {
            lonMin = -180;
        }
        if (lonMax > 170) {
            lonMax = 180;
        }
        double delta = (lonMax - lonMin) / (this.nPoints - 1);
        for (double lat : this.latitudeLocations) {
            List<PointD> points = new ArrayList<>();
            double lon = lonMin;
            while (lon <= lonMax) {
                points.add(new PointD(lon, lat));
                lon += delta;
            }
            PolylineShape line = new PolylineShape();
            line.setPoints(points);
            Graphic graphic = new Graphic(line, this.lineBreak);
            graphic = ProjectionUtil.projectClipGraphic(graphic, longLat, projInfo);
            if (graphic.getNumGraphics() > 1) {
                points = (List<PointD>) graphic.getGraphicN(0).getShape().getPoints();
                List<PointD> points1 = (List<PointD>) graphic.getGraphicN(1).getShape().getPoints();
                Collections.reverse(points1);
                points.addAll(points1);
                line = new PolylineZShape();
                line.setPoints(points);
                graphic = new Graphic(line, this.lineBreak);
            }
            graphic.getShape().setValue(lat);
            this.latitudeLines.add(graphic);
        }
    }

    protected void updateLonLatGridLabels() {
        //Longitude
        List<GridLabel> tLabels = new ArrayList<>();
        for (int i = 0; i < this.longitudeLines.size(); i++) {
            PolylineShape line = (PolylineShape) this.longitudeLines.getGraphicN(i).getShape();
            float value = (float) line.getValue();
            String labStr = String.valueOf(value);
            labStr = DataConvert.removeTailingZeros(labStr);

            if (value == -180) {
                labStr = "180";
            } else if (!(value == 0 || value == 180)) {
                if (labStr.substring(0, 1).equals("-")) {
                    labStr = labStr.substring(1) + "W";
                } else {
                    labStr = labStr + "E";
                }
            }

            List<GridLabel> gLabels = new ArrayList<>();
            for (Polyline aPL : line.getPolylines()) {
                gLabels.addAll(GeoComputation.getGridLabels(aPL, extent, true));
            }

            for (GridLabel gLabel : gLabels) {
                gLabel.setLabString(labStr);
                gLabel.setValue(value);
            }

            tLabels.addAll(gLabels);
        }

        //Latitude
        for (int i = 0; i < this.latitudeLines.size(); i++) {
            PolylineShape line = (PolylineShape) this.latitudeLines.getGraphicN(i).getShape();
            float value = (float) line.getValue();
            String labStr = String.valueOf(value);
            labStr = DataConvert.removeTailingZeros(labStr);

            if (value == 90 || value == -90) {
                continue;
            }

            if (!(value == 0)) {
                if (labStr.substring(0, 1).equals("-")) {
                    labStr = labStr.substring(1) + "S";
                } else {
                    labStr = labStr + "N";
                }
            }

            List<GridLabel> gLabels = new ArrayList<>();
            for (Polyline aPL : line.getPolylines()) {
                gLabels.addAll(GeoComputation.getGridLabels(aPL, extent, false));
            }

            for (GridLabel gLabel : gLabels) {
                gLabel.setLabString(labStr);
                gLabel.setValue(value);
            }

            tLabels.addAll(gLabels);
        }

        //Adjust for different projections
        float refLon;
        gridLabels = new ArrayList<>();
        switch (projInfo.getProjectionName()) {
            case Lambert_Conformal_Conic:
                for (GridLabel aGL : tLabels) {
                    if (!aGL.isBorder()) {
                        if (!aGL.isLongitude()) {
                            aGL.setLabDirection(Direction.North);
                        } else {
                            if (aGL.getCoord().Y > 0 && Math.abs(aGL.getCoord().X) < 1000) {
                                continue;
                            }

                            if (MIMath.lonDistance(aGL.getValue(), (float) projInfo.getCenterLon()) > 60) {
                                if (aGL.getCoord().X < 0) {
                                    aGL.setLabDirection(Direction.Weast);
                                } else {
                                    aGL.setLabDirection(Direction.East);
                                }
                            } else {
                                aGL.setLabDirection(Direction.South);
                            }
                        }
                    }
                    gridLabels.add(aGL);
                }
                break;
            case Albers_Equal_Area:
            case Lambert_Equal_Area_Conic:
                for (GridLabel aGL : tLabels) {
                    if (!aGL.isBorder()) {
                        if (!aGL.isLongitude()) {
                            aGL.setLabDirection(Direction.North);
                        } else {
                            if (aGL.getCoord().Y > 7000000 && Math.abs(aGL.getCoord().X) < 5000000) {
                                continue;
                            }

                            if (MIMath.lonDistance(aGL.getValue(), (float) projInfo.getCenterLon()) > 60) {
                                if (aGL.getCoord().X < 0) {
                                    aGL.setLabDirection(Direction.Weast);
                                } else {
                                    aGL.setLabDirection(Direction.East);
                                }
                            } else {
                                aGL.setLabDirection(Direction.South);
                            }
                        }
                    }
                    gridLabels.add(aGL);
                }
                break;
            case Mercator:
                for (GridLabel gl : tLabels) {
                    if (!gl.isBorder()) {
                        if (gl.isLongitude()) {
                            if (gl.getCoord().Y > 1000) {
                                gl.setLabDirection(Direction.North);
                            }
                        }
                    }
                    gridLabels.add(gl);
                }
                break;
            case North_Polar_Stereographic_Azimuthal:
            case South_Polar_Stereographic_Azimuthal:
                for (GridLabel aGL : tLabels) {
                    if (!aGL.isBorder()) {
                        if (aGL.isLongitude()) {
                            if (Math.abs(aGL.getCoord().X) < 1000 && Math.abs(aGL.getCoord().Y) < 1000) {
                                continue;
                            }

                            refLon = (float) projInfo.getCenterLon();
                            if (MIMath.lonDistance(aGL.getValue(), refLon) < 45) {
                                if (projInfo.getProjectionName() == ProjectionNames.North_Polar_Stereographic_Azimuthal) {
                                    aGL.setLabDirection(Direction.South);
                                } else {
                                    aGL.setLabDirection(Direction.North);
                                }
                            } else {
                                refLon = MIMath.lonAdd(refLon, 180);
                                if (MIMath.lonDistance(aGL.getValue(), refLon) < 45) {
                                    if (projInfo.getProjectionName() == ProjectionNames.North_Polar_Stereographic_Azimuthal) {
                                        aGL.setLabDirection(Direction.North);
                                    } else {
                                        aGL.setLabDirection(Direction.South);
                                    }
                                } else if (aGL.getCoord().X < 0) {
                                    aGL.setLabDirection(Direction.Weast);
                                } else {
                                    aGL.setLabDirection(Direction.East);
                                }
                            }
                        } else {
                            continue;
                        }
                    }

                    gridLabels.add(aGL);
                }
                break;
            case Robinson:
                for (GridLabel aGL : tLabels) {
                    if (!aGL.isBorder()) {
                        if (aGL.isLongitude()) {
                            if (aGL.getCoord().Y < 0) {
                                aGL.setLabDirection(Direction.South);
                            } else {
                                aGL.setLabDirection(Direction.North);
                            }
                        } else if (aGL.getCoord().X < 0) {
                            aGL.setLabDirection(Direction.Weast);
                        } else {
                            aGL.setLabDirection(Direction.East);
                        }
                    }

                    gridLabels.add(aGL);
                }
                break;
            case Molleweide:
            case Hammer_Eckert:
                for (GridLabel aGL : tLabels) {
                    if (!aGL.isBorder()) {
                        if (aGL.isLongitude()) {
                            continue;
                        } else if (aGL.getCoord().X < 0) {
                            aGL.setLabDirection(Direction.Weast);
                        } else {
                            aGL.setLabDirection(Direction.East);
                        }
                    }

                    gridLabels.add(aGL);
                }
                break;
            case Orthographic_Azimuthal:
            case Geostationary_Satellite:
                for (GridLabel aGL : tLabels) {
                    if (!aGL.isBorder()) {
                        if (aGL.isLongitude()) {
                            continue;
                        } else if (aGL.getCoord().X < 0) {
                            aGL.setLabDirection(Direction.Weast);
                        } else {
                            aGL.setLabDirection(Direction.East);
                        }
                    }

                    gridLabels.add(aGL);
                }
                break;
            case Oblique_Stereographic_Alternative:
            case Transverse_Mercator:
                for (GridLabel aGL : tLabels) {
                    if (!aGL.isBorder()) {
                        continue;
                    }

                    gridLabels.add(aGL);
                }
                break;
            default:
                gridLabels = tLabels;
                break;
        }
    }

    /**
     * Get longitude labels
     * @return Longitude labels
     */
    public List<GridLabel> getLongitudeLabels() {
        List<GridLabel> lonLabels = new ArrayList<>();
        for (GridLabel label : this.gridLabels) {
            if (label.isLongitude() && label.getLabDirection() == Direction.South) {
                lonLabels.add(label);
            }
        }

        return lonLabels;
    }

    /**
     * Get latitude labels
     * @return Latitude labels
     */
    public List<GridLabel> getLatitudeLabels() {
        List<GridLabel> latLabels = new ArrayList<>();
        for (GridLabel label : this.gridLabels) {
            if (!label.isLongitude() && label.getLabDirection() == Direction.Weast) {
                latLabels.add(label);
            }
        }

        return latLabels;
    }
}
