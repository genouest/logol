package org.irisa.genouest.logol.utils;

import java.io.IOException;
import java.io.OutputStream;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.Namespace;
import org.biojavax.bio.BioEntry;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.io.FastaFormat;
import org.biojavax.bio.seq.io.FastaHeader;
import org.biojavax.bio.seq.io.RichStreamWriter;

@Deprecated
public class FastaConverter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	

	
    /**
     * Writes <CODE>Sequence</CODE>s from a <code>SequenceIterator</code> to an <code>OutputStream </code>in
     * Fasta Format.  This makes for a useful format filter where a
     * <code>StreamReader</code> can be sent to the <code>RichStreamWriter</code> after formatting.
     * @param os The stream to write fasta formatted data to
     * @param in The source of input <CODE>RichSequence</CODE>s
     * @param ns a <code>Namespace</code> to write the <CODE>RichSequence</CODE>s to. <CODE>Null</CODE> implies that it should
     * use the namespace specified in the individual sequence.
     * @param length Size of the sequence line.
     * @throws java.io.IOException if there is an IO problem
     */
    public synchronized static void writeFasta(OutputStream os, SequenceIterator in, Namespace ns, FastaHeader header,int length)
    throws IOException {
        FastaFormat fastaFormat = new FastaFormat();
        fastaFormat.setLineWidth(length);
        if(header != null){
            fastaFormat.setHeader(header);
        }

        RichStreamWriter sw = new RichStreamWriter(os,fastaFormat);        
        sw.writeStream(in,ns);       
    }
    
    
    /**
     * Writes a single <code>Sequence</code> to an <code>OutputStream</code> in Fasta format.
     * @param os the <code>OutputStream</code>.
     * @param seq the <code>Sequence</code>.
     * @param ns a <code>Namespace</code> to write the sequences to. Null implies that it should
     *              use the namespace specified in the individual sequence.
     * @throws java.io.IOException if there is an IO problem
     */
    public synchronized static void writeFasta(OutputStream os, Sequence seq, Namespace ns)
    throws IOException {
        writeFasta(os, new org.biojavax.bio.seq.RichSequence.IOTools.SingleRichSeqIterator(seq),ns, null,seq.length());
    }
	

}
