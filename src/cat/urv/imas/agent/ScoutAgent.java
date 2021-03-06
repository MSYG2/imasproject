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
import cat.urv.imas.map.CellType;
import cat.urv.imas.onthology.MessageContent;
import jade.core.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.*;

/**
 * The Scout agent. 
 * TODO: Agent responsibilities: can bring several types of garbage, 
 * but only a single type of garbage at a time.
 */
public class ScoutAgent extends ImasAgent {

    /**
     * Game settings in use.
     */
    private GameSettings game;
    /**
     * Scout Coordinator agent id.
     */
    private AID scoutCoordinatorAgent;
    /**
     * Row number for this agent, zero based.
     */
    private int row = -1;
    /**
     * Column number for this agent, zero based.
     */
    private int col = -1;
    
    /**
     * Builds the scout agent.
     */
    public ScoutAgent() {
        super(AgentType.SCOUT);
    }
    
    /**
     * Builds the scout agent.
     */
    public ScoutAgent(Object[] arguments) {
        super(AgentType.SCOUT);
        this.row = Integer.parseInt(arguments[0].toString());
        this.col = Integer.parseInt(arguments[1].toString());
    }

    /**
     * Builds the scout agent.
     */
    public ScoutAgent(int row, int col) {
        super(AgentType.SCOUT);
        this.row = row;
        this.col = col;
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
        
        // search Scout Coordinator Agent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.SCOUT_COORDINATOR.toString());
        this.scoutCoordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
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
                + "(c " + this.getCol() + ")";
        return str + ")";
    }

}
