/*
 * MATLAB Compiler: 7.0 (R2018b)
 * Date: Tue Apr  9 13:10:53 2019
 * Arguments: 
 * "-B""macro_default""-v""-K""-W""java:MReportUtils,MReportUtils""-T""link:lib""-d""C:\\Users\\DeMarco-PC\\JPAD_PROJECT\\jpad\\JPADReport\\matlab\\dev\\MReportUtils\\for_testing""class{MReportUtils:C:\\Users\\DeMarco-PC\\JPAD_PROJECT\\jpad\\JPADReport\\matlab\\dev\\closeChapter.m,C:\\Users\\DeMarco-PC\\JPAD_PROJECT\\jpad\\JPADReport\\matlab\\dev\\closeReport.m,C:\\Users\\DeMarco-PC\\JPAD_PROJECT\\jpad\\JPADReport\\matlab\\dev\\makeChapter.m,C:\\Users\\DeMarco-PC\\JPAD_PROJECT\\jpad\\JPADReport\\matlab\\dev\\makeFigure.m,C:\\Users\\DeMarco-PC\\JPAD_PROJECT\\jpad\\JPADReport\\matlab\\dev\\makeParagraph.m,C:\\Users\\DeMarco-PC\\JPAD_PROJECT\\jpad\\JPADReport\\matlab\\dev\\makeReport.m,C:\\Users\\DeMarco-PC\\JPAD_PROJECT\\jpad\\JPADReport\\matlab\\dev\\makeSection.m,C:\\Users\\DeMarco-PC\\JPAD_PROJECT\\jpad\\JPADReport\\matlab\\dev\\makeTable.m}""-a""C:\\Users\\DeMarco-PC\\JPAD_PROJECT\\jpad\\JPADReport\\matlab\\dev\\DAF_template.pdftx"
 */

package MReportUtils;

import com.mathworks.toolbox.javabuilder.*;
import com.mathworks.toolbox.javabuilder.internal.*;

/**
 * <i>INTERNAL USE ONLY</i>
 */
public class MReportUtilsMCRFactory
{
   
    
    /** Component's uuid */
    private static final String sComponentId = "MReportUtils_ECEE3C2D61BAF6521087FE44FB25F003";
    
    /** Component name */
    private static final String sComponentName = "MReportUtils";
    
   
    /** Pointer to default component options */
    private static final MWComponentOptions sDefaultComponentOptions = 
        new MWComponentOptions(
            MWCtfExtractLocation.EXTRACT_TO_CACHE, 
            new MWCtfClassLoaderSource(MReportUtilsMCRFactory.class)
        );
    
    
    private MReportUtilsMCRFactory()
    {
        // Never called.
    }
    
    public static MWMCR newInstance(MWComponentOptions componentOptions) throws MWException
    {
        if (null == componentOptions.getCtfSource()) {
            componentOptions = new MWComponentOptions(componentOptions);
            componentOptions.setCtfSource(sDefaultComponentOptions.getCtfSource());
        }
        return MWMCR.newInstance(
            componentOptions, 
            MReportUtilsMCRFactory.class, 
            sComponentName, 
            sComponentId,
            new int[]{9,5,0}
        );
    }
    
    public static MWMCR newInstance() throws MWException
    {
        return newInstance(sDefaultComponentOptions);
    }
}
