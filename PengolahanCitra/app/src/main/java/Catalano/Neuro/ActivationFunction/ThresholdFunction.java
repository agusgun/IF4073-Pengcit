// Catalano Machine Learning Library
// The Catalano Framework
//
// Copyright © Diego Catalano, 2012-2016
// diego.catalano at live.com
//
// Copyright © Andrew Kirillov, 2007-2008
// andrew.kirillov at gmail.com
//
//    This library is free software; you can redistribute it and/or
//    modify it under the terms of the GNU Lesser General Public
//    License as published by the Free Software Foundation; either
//    version 2.1 of the License, or (at your option) any later version.
//
//    This library is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//    Lesser General Public License for more details.
//
//    You should have received a copy of the GNU Lesser General Public
//    License along with this library; if not, write to the Free Software
//    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//

package Catalano.Neuro.ActivationFunction;

/**
 * Threshold activation function.
 * @author Diego Catalano
 */
public class ThresholdFunction implements IActivationFunction{

    /**
     * Initializes a new instance of the ThresholdFunction class.
     */
    public ThresholdFunction() {}

    @Override
    public double Function(double x) {
        return ( x >= 0 ) ? 1 : 0;
    }
    
    @Override
    public double Derivative(double x){
        return 0;
    }
    
    public double Derivative2(double y){
        return 0;
    }
    
}