package de.mpicbg.knime.scripting.r.generic;

import java.io.File;
import java.util.Collections;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import de.mpicbg.knime.scripting.core.exceptions.KnimeScriptingException;
import de.mpicbg.knime.scripting.r.AbstractRScriptingNodeModel;
import de.mpicbg.knime.scripting.r.OpenInRNodeModel;
import de.mpicbg.knime.scripting.r.RColumnSupport;
import de.mpicbg.knime.scripting.r.RUtils;
import de.mpicbg.knime.scripting.r.node.snippet.RSnippetNodeModel;


/**
 * This is the model implementation of generic version of OpenInR. It allows to spawn a new instance of R using the node
 * input as workspace initialization. It's main purpose is for prototyping.
 *
 * @author Holger Brandl (MPI-CBG)
 * @deprecated
 */
public class GenericOpenInRNodeModel extends AbstractRScriptingNodeModel {

	/* (non-Javadoc)
	 * @see de.mpicbg.knime.scripting.core.AbstractScriptingNodeModel#hasOutput()
	 */
	@Override
	protected boolean hasOutput() {
		// TODO Auto-generated method stub
		return false;
	}
	
    @Override
	/* (non-Javadoc)
	 * @see org.knime.core.node.NodeModel.configure(final PortObjectSpec[] inSpecs)
	 */
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        return new PortObjectSpec[0];
    }

    /**
     * @deprecated
     */
	protected GenericOpenInRNodeModel() {
        super(createPorts(1, RPortObject.TYPE, RPortObject.class), new PortType[0], new RColumnSupport());
    }
   
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] executeImpl(PortObject[] inData,
			ExecutionContext exec) throws Exception {
		//create connection to server
    	logger.info("Creating R-connection");
    	m_con = RUtils.createConnection();
        
        File rWorkspaceFile = null;
        try {
        // push incoming data to R server
        logger.info("Pushing inputs to R...");
        RUtils.loadGenericInputs(Collections.singletonMap(RSnippetNodeModel.R_INVAR_BASE_NAME, ((RPortObject)inData[0]).getFile()), m_con);
        
        // save workspace file and return it to local machine
        rWorkspaceFile = File.createTempFile("genericR", RSnippetNodeModel.R_INVAR_BASE_NAME);
        RUtils.saveToLocalFile(rWorkspaceFile, m_con, RUtils.getHost(), RSnippetNodeModel.R_INVAR_BASE_NAME);
        } catch(Exception e) {
        	closeRConnection();
        	throw e;
        }
        
        // close connection to server
        closeRConnection();
        
        // open R with workspace file
        logger.info("Spawning R-instance ...");
        OpenInRNodeModel.openWSFileInR(rWorkspaceFile, prepareScript());

        return new PortObject[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void openIn(PortObject[] inData, ExecutionContext exec)
			throws KnimeScriptingException {
		try {
			executeImpl(inData, exec);
		} catch (Exception e) {
			throw new KnimeScriptingException(e.getMessage());
		}
	}
}