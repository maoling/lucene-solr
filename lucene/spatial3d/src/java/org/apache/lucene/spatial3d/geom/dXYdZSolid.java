/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.spatial3d.geom;

/**
 * 3D rectangle, bounded on six sides by X,Y,Z limits, degenerate in X and Z.
 * This figure, in fact, represents either zero, one, or two points, so the
 * actual data stored is minimal.
 *
 * @lucene.internal
 */
public class dXYdZSolid extends BaseXYZSolid {

  /** The points in this figure on the planet surface; also doubles for edge points */
  protected final GeoPoint[] surfacePoints;
  
  /**
   * Sole constructor
   *
   *@param planetModel is the planet model.
   *@param X is the X value.
   *@param minY is the minimum Y value.
   *@param maxY is the maximum Y value.
   *@param Z is the Z value.
   */
  public dXYdZSolid(final PlanetModel planetModel,
    final double X,
    final double minY,
    final double maxY,
    final double Z) {
    super(planetModel);
    // Argument checking
    if (maxY - minY < Vector.MINIMUM_RESOLUTION)
      throw new IllegalArgumentException("Y values in wrong order or identical");

    // Build the planes and intersect them.
    final Plane xPlane = new Plane(xUnitVector,-X);
    final Plane zPlane = new Plane(zUnitVector,-Z);
    final SidedPlane minYPlane = new SidedPlane(0.0,maxY,0.0,yUnitVector,-minY);
    final SidedPlane maxYPlane = new SidedPlane(0.0,minY,0.0,yUnitVector,-maxY);
    surfacePoints = xPlane.findIntersections(planetModel,zPlane,minYPlane,maxYPlane);
  }

  @Override
  protected GeoPoint[] getEdgePoints() {
    return surfacePoints;
  }
  
  @Override
  public boolean isWithin(final double x, final double y, final double z) {
    for (final GeoPoint p : surfacePoints) {
      if (p.isIdentical(x,y,z))
        return true;
    }
    return false;
  }

  @Override
  public int getRelationship(final GeoShape path) {
    //System.err.println(this+" getrelationship with "+path);
    final int insideRectangle = isShapeInsideArea(path);
    if (insideRectangle == SOME_INSIDE) {
      //System.err.println(" some inside");
      return OVERLAPS;
    }

    // Figure out if the entire XYZArea is contained by the shape.
    final int insideShape = isAreaInsideShape(path);
    if (insideShape == SOME_INSIDE) {
      return OVERLAPS;
    }

    if (insideRectangle == ALL_INSIDE && insideShape == ALL_INSIDE) {
      //System.err.println(" inside of each other");
      return OVERLAPS;
    }

    if (insideRectangle == ALL_INSIDE) {
      return WITHIN;
    }
    
    if (insideShape == ALL_INSIDE) {
      //System.err.println(" shape contains rectangle");
      return CONTAINS;
    }
    //System.err.println(" disjoint");
    return DISJOINT;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof dXYdZSolid))
      return false;
    dXYdZSolid other = (dXYdZSolid) o;
    if (!super.equals(other) || surfacePoints.length != other.surfacePoints.length ) {
      return false;
    }
    for (int i = 0; i < surfacePoints.length; i++) {
      if (!surfacePoints[i].equals(other.surfacePoints[i]))
        return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    for (final GeoPoint p : surfacePoints) {
      result = 31 * result  + p.hashCode();
    }
    return result;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for (final GeoPoint p : surfacePoints) {
      sb.append(" ").append(p).append(" ");
    }
    return "dXYdZSolid: {planetmodel="+planetModel+", "+sb.toString()+"}";
  }
  
}
  
