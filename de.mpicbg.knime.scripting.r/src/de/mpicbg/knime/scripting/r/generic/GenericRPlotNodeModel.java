package de.mpicbg.knime.scripting.r.generic;

import java.io.IOException;

import de.mpicbg.knime.scripting.core.AbstractScriptingNodeModel;
import de.mpicbg.knime.scripting.core.ScriptProvider;
import de.mpicbg.knime.scripting.core.exceptions.KnimeScriptingException;
import de.mpicbg.knime.scripting.r.RUtils;
import de.mpicbg.knime.scripting.r.plots.AbstractRPlotNodeModel;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;


/**
 * A generic R node which creates a figure from a genericR input.
 *
 * @author Holger Brandl
 */
public class GenericRPlotNodeModel extends AbstractRPlotNodeModel {


    public GenericRPlotNodeModel() {
        super(createPorts(1, ROldPortObject.TYPE, ROldPortObject.class));
    }


    /**
     * This constructor is just necessary to allow superclasses to create plot nodes with ouputs
     */
    public GenericRPlotNodeModel(PortType[] inPorts, PortType[] outports) {
        super(inPorts, outports);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
    	super.configure(inSpecs);
        return new PortObjectSpec[0];
    }


    /**
     * Prepares the ouput tables of this nodes. As most plot-nodes won't have any data output, this method won't be
     * overridden in most cases. Just in case a node should have both (an review and a data table output), you may
     * override it.
     */
    protected PortObject[] prepareOutput(ExecutionContext exec, RConnection connection) {
        return new BufferedDataTable[0];
    }

    /**
     * {@inheritDoc}
     */
	@Override
	protected PortObject[] executeImpl(PortObject[] inData,
			ExecutionContext exec) throws Exception {
		RConnection connection = RUtils.createConnection();
        PortObject[] nodeOutput = null;
        try {
	        // 1) restore the workspace in a different server session
	        RUtils.pushToR(inData, connection, exec, AbstractScriptingNodeModel.CHUNK_IN_DFT);
	
	        // 2) create the figure
	        adaptHardwiredTemplateToContext(ScriptProvider.unwrapPortSpecs(inData));
	        createFigure(connection);
	
	
	        // 3) prepare the output tables (which will do nothing in most cases, as most plot nodes don't have output)
	        nodeOutput = prepareOutput(exec, connection);
	
	        // 3) close the connection to R (which will also delete the temporary workspace on the server)
	        connection.close();
        } catch(Exception e) {
        	connection.close();
        	throw e;
        }

        return nodeOutput;
	}

    /**
     * {@inheritDoc}
     */
	@Override
	protected void openIn(PortObject[] inData, ExecutionContext exec)
			throws KnimeScriptingException {
		try {
			String rawScript = prepareScript();
			RUtils.openInR(inData, exec, rawScript, logger);   
			setWarningMessage("To push the node's input to R again, you need to reset and re-execute it.");
		} catch (REXPMismatchException | IOException | REngineException e) {
			throw new KnimeScriptingException("Failed to open in R\n" + e);
		}
	}
}
