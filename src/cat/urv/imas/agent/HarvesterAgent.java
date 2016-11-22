/**
 *  IMAS base code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.agent;

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.behaviour.coordinator.RequesterBehaviour;
import cat.urv.imas.onthology.GarbageType;
import cat.urv.imas.onthology.MessageContent;
import jade.core.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.*;

/**
 * The Harvester agent. 
 * TODO: Agent responsibilities: can bring several types of garbage, 
 * but only a single type of garbage at a time.
 */
public class HarvesterAgent extends ImasAgent {

    /**
     * Game settings in use.
     */
    private GameSettings game;
    /**
     * Harvester Paper Coordinator agent id.
     */
    private AID harvesterPaperCoordinatorAgent;
    /**
     * Harvester Glass Coordinator agent id.
     */
    private AID harvesterGlassCoordinatorAgent;
    /**
     * Harvester Plastic Coordinator agent id.
     */
    private AID harvesterPlasticCoordinatorAgent;
    /**
     * Row number for this agent, zero based.
     */
    private int row = -1;
    /**
     * Column number for this agent, zero based.
     */
    private int col = -1;
    /**
     * Types of garbage allowed to harvest.
     */
    protected GarbageType[] allowedTypes;
    /**
     * Maximum units of garbage of any type able to harvest at a time.
     */
    protected int capacity;
    

    /**
     * Builds the harvester agent.
     */
    public HarvesterAgent(int row, int col, int capacity, GarbageType[] allowedTypes) {
        super(AgentType.HARVESTER);
        this.row = row;
        this.col = col;
        this.capacity = capacity;
        this.allowedTypes = allowedTypes;
    }

    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {

        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.HARVESTER_COORDINATOR.toString());
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);
        
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd1);
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
            log("Registered to the DF");
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " registration with DF unsucceeded. Reason: " + e.getMessage());
            doDelete();
        }
        
        // search Harvester Paper Coordinator Agent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.HARVESTER_PAPER_COORDINATOR.toString());
        this.harvesterPaperCoordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        // searchAgent is a blocking method, so we will obtain always a correct AID
        
        // search Harvester Paper Coordinator Agent
        searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.HARVESTER_GLASS_COORDINATOR.toString());
        this.harvesterGlassCoordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        // searchAgent is a blocking method, so we will obtain always a correct AID
        
        // search Harvester Plastic Coordinator Agent
        searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.HARVESTER_PLASTIC_COORDINATOR.toString());
        this.harvesterPlasticCoordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        // searchAgent is a blocking method, so we will obtain always a correct AID
    }

    /**
     * Update the game settings.
     *
     * @param game current game settings.
     */
    public void setGame(GameSettings game) {
        this.game = game;
    }

    /**
     * Gets the current game settings.
     *
     * @return the current game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }
    
    /* ********************************************************************** */
    /**
     * Gets the current row.
     *
     * @return the current row number in the map, in zero base.
     */
    public int getRow() {
        return this.row;
    }

    /**
     * Gets the current column number in the map, in zero base.
     *
     * @return Column number in the map, in zero base.
     */
    public int getCol() {
        return this.col;
    }
    
    /**
     * Gets capacity for harvester.
     *
     * @return harvester capacity.
     */
    public int getCapacity() {
        return this.capacity;
    }
    
    /**
     * check if Garbage Type Allowed.
     *
     * @return true if type is allowed or return false if not.
     */
    public boolean isAllowedType(GarbageType type){
        for(int i = 0;i < allowedTypes.length;i++)
            if(allowedTypes[i] == type)
                return true;
        return false;
    }

    /* ********************************************************************** */
    /**
     * Gets a string representation of the cell.
     *
     * @return
     */
    @Override
    public String toString() {
        String str = "(Agent-name " + this.getAID() + " "
                + "(r " + this.getRow() + ")"
                + "(c " + this.getCol() + ")" 
                + " Agent Capacity: " + getCapacity();
        return str + ")";
    }

}
