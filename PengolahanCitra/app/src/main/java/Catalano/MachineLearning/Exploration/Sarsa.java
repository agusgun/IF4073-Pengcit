// Catalano Machine Learning Library
// The Catalano Framework
//
// Copyright © Diego Catalano, 2012-2016
// diego.catalano at live.com
//
// Copyright © Andrew Kirillov, 2007-2008
// andrew.kirillov@gmail.com
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

package Catalano.MachineLearning.Exploration;

import java.util.Random;

/**
 * The class provides implementation of Sarse algorithm, known as on-policy Temporal Difference control.
 * @author Diego Catalano
 */
public class Sarsa {
    // amount of possible states
    private int states;
    // amount of possible actions
    private int actions;
    // q-values
    private double[][] qvalues;
    // exploration policy
    private IExplorationPolicy explorationPolicy;

    // discount factor
    private double discountFactor = 0.95;
    // learning rate
    private double learningRate = 0.25;

    /**
     * Amount of possible states.
     * @return States
     */
    public int getStates() {
        return states;
    }

    /**
     * Amount of possible actions.
     * @return Actions
     */
    public int getActions() {
        return actions;
    }

    /**
     * Exploration policy.
     * @return Exploration policy.
     */
    public IExplorationPolicy getExplorationPolicy() {
        return explorationPolicy;
    }

    /**
     * Exploration policy.
     * Policy, which is used to select actions.
     * @param explorationPolicy Exploration policy
     */
    public void setExplorationPolicy(IExplorationPolicy explorationPolicy) {
        this.explorationPolicy = explorationPolicy;
    }

    /**
     * Get learning rate
     * @return Learning rate
     */
    public double getLearningRate() {
        return learningRate;
    }

    /**
     * Learning rate, [0, 1].
     * The value determines the amount of updates Q-function receives
     * during learning. The greater the value, the more updates the function receives.
     * The lower the value, the less updates it receives.
     * @param learningRate Learning rate
     */
    public void setLearningRate(double learningRate) {
        this.learningRate = Math.max(0.0, Math.min(1.0, learningRate));
    }

    /**
     * Get Discount Factor
     * @return Discount Factor
     */
    public double getDiscountFactor() {
        return discountFactor;
    }

    /**
     * Discount factor, [0, 1].
     * Discount factor for the expected summary reward. The value serves as
     * multiplier for the expected reward. So if the value is set to 1,
     * then the expected summary reward is not discounted. If the value is getting
     * smaller, then smaller amount of the expected reward is used for actions'
     * estimates update.
     * @param discountFactor Discount Factor
     */
    public void setDiscountFactor(double discountFactor) {
        this.discountFactor = Math.max(0.0, Math.min(1.0, discountFactor));
    }
    
    /**
     * Initializes a new instance of the Sarsa class.
     * @param states Amount of possible states.
     * @param actions Amount of possible actions.
     * @param explorationPolicy Exploration policy.
     * @param randomize Randomize action estimates or not.
     * 
     * The <b>randomize</b> parameter specifies if initial action estimates should be randomized
     * with small values or not. Randomization of action values may be useful, when greedy exploration
     * policies are used. In this case randomization ensures that actions of the same type are not chosen always.
     */
    public Sarsa( int states, int actions, IExplorationPolicy explorationPolicy, boolean randomize ){
        this.states = states;
        this.actions = actions;
        this.explorationPolicy = explorationPolicy;

        // create Q-array
        qvalues = new double[states][];
        for ( int i = 0; i < states; i++ ){
            qvalues[i] = new double[actions];
        }

        // do randomization
        if ( randomize ){
            Random r = new Random( );

            for ( int i = 0; i < states; i++ ){
                for ( int j = 0; j < actions; j++ ){
                    qvalues[i][j] = r.nextDouble() / 10;
                }
            }
        }
    }
    
    /**
     * Get next action from the specified state.
     * @param state Current state to get an action for.
     * @return Returns the action for the state.
     */
    public int GetAction( int state ){
        return explorationPolicy.ChooseAction( qvalues[state] );
    }
    
    /**
     * Update Q-function's value for the previous state-action pair.
     * @param previousState Current state.
     * @param previousAction Action, which lead from previous to the next state.
     * @param reward Reward value, received by taking specified action from previous state.
     * @param nextState Next state.
     * @param nextAction Next action.
     */
    public void UpdateState( int previousState, int previousAction, double reward, int nextState, int nextAction ){
        // previous state's action estimations
        double[] previousActionEstimations = qvalues[previousState];
        // update expexted summary reward of the previous state
        previousActionEstimations[previousAction] *= ( 1.0 - learningRate );
        previousActionEstimations[previousAction] += ( learningRate * ( reward + discountFactor * 
                                                        qvalues[nextState][nextAction] ) );

    }
    
    /**
     * Update Q-function's value for the previous state-action pair.
     * @param previousState Current state.
     * @param previousAction Action, which lead from previous to the next state.
     * @param reward Reward value, received by taking specified action from previous state.
     * 
     * Updates Q-function's value for the previous state-action pair in
     * the case if the next state is terminal.
     */
    public void UpdateState( int previousState, int previousAction, double reward ){
        // previous state's action estimations
        double[] previousActionEstimations = qvalues[previousState];
        // update expexted summary reward of the previous state
        previousActionEstimations[previousAction] *= ( 1.0 - learningRate );
        previousActionEstimations[previousAction] += ( learningRate * reward );
    }
    
}
