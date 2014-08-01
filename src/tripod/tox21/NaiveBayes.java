package tripod.tox21;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;

import lychi.LyChIStandardizer;
import tripod.fingerprint.PCFP;

/**
 * This class read in the Tox21 challenge SD file (tox21_10k_data_all.sdf)
 * and builds 12 Naive Bayes models for each assay based on the NCATS' 
 * implementation of the PubChem fingerprint.
 */
public class NaiveBayes {
    static final Logger logger = 
        Logger.getLogger(NaiveBayes.class.getName());

    private List<BayesModel> models = new ArrayList<BayesModel>();

    public NaiveBayes (String... models) {
        for (String m : models) {
            this.models.add(new BayesModel (m));
        }
    }

    public NaiveBayes (File dir) throws IOException {
        if (!dir.isDirectory()) 
            throw new IllegalArgumentException
                (dir.getPath()+" is not a directory!");

        for (File f : dir.listFiles()) {
            logger.info("Loading model "+f.getName()+"...");
            BayesModel model = BayesModel.load(f);
            //model.write(System.out);
            models.add(model);
        }
    }

    public void train (InputStream is) throws IOException {
        MolImporter mi = new MolImporter (is);

        LyChIStandardizer lychi = new LyChIStandardizer ();
        PCFP pcfp = new PCFP ();
        for (Molecule mol; (mol = mi.read()) != null; ) {
            try {
                // standardize before fingerprint generation
                lychi.standardize(mol);
            }
            catch (Exception ex) {
                logger.warning("Can't standardize "+mol.getName());
            }

            // now generate the fingerprint
            pcfp.setMolecule(mol);

            BitSet bits = pcfp.toBits();
            for (BayesModel bm : models) {
                String prop = mol.getProperty(bm.getName());
                if (prop != null) {
                    try {
                        int active = Integer.parseInt(prop);
                        bm.add(active > 0, bits);
                    }
                    catch (NumberFormatException ex) {
                        logger.log(Level.SEVERE, "Bogus activity for "
                                   +bm.getName()+": "+prop);
                    }
                }
            }
        }
    }

    public void predict (String file) throws IOException {
        predict (new FileInputStream (file));
    }

    public void predict (InputStream is) throws IOException {
        LyChIStandardizer lychi = new LyChIStandardizer ();
        PCFP pcfp = new PCFP ();

        MolImporter mi = new MolImporter (is);

        // print header
        Map<BayesModel, PrintStream> streams = 
            new HashMap<BayesModel, PrintStream>();
        for (BayesModel m : models) {
            PrintStream ps = new PrintStream
                (new FileOutputStream (m.getName()+".txt"));
            ps.println("Sample ID\tScore\tActivity");
            streams.put(m, ps);
        }

        for (Molecule mol = new Molecule (); mi.read(mol); ) {
            try {
                // standardize before fingerprint generation
                lychi.standardize(mol);
            }
            catch (Exception ex) {
                logger.warning("Can't standardize "+mol.getName());
            }
            
            // now generate the fingerprint
            pcfp.setMolecule(mol);
            
            BitSet bits = pcfp.toBits();
            for (BayesModel model : models) {
                double prob = model.getPosterior(bits);

                PrintStream ps = streams.get(model);
                ps.print(mol.getName());
                ps.print("\t"+String.format("%1.2f", prob)
                         +"\t" + (prob >= .5 ? "1" : "0"));
                ps.println();
            }
        }

        for (PrintStream ps : streams.values())
            ps.close();
    }

    public List<BayesModel> getModels () {
        return Collections.unmodifiableList(models);
    }

    public static class Train {
        public static void main (String[] argv) throws Exception {
            if (argv.length == 0) {
                System.err.println("Usage: "+Train.class+" FILES...");
                System.exit(1);
            }
            
            NaiveBayes nb = new NaiveBayes 
                ("NR-AHR","NR-AR-LBD","NR-AR","NR-AROMATASE","NR-ER-LBD",
                 "NR-ER","NR-PPAR-GAMMA","SR-ARE","SR-ATAD5","SR-HSE",
                 "SR-MMP", "SR-P53");
            
            for (String a : argv) {
                nb.train(new FileInputStream (a));
                //nb.predict(a);
            }
            
            for (BayesModel bm : nb.getModels()) {
                logger.info("Saving model "+bm);
                bm.bitSelection(30);
                bm.save();
            }
        }
    }

    public static class Predict {
        public static void main (String[] argv) throws Exception {
            if (argv.length < 2) {
                System.err.println("Usage: "+Predict.class+" DIR FILES...");
                System.err.println("where DIR is the directory contains "
                                   +"models generated by NaiveBayes$Train");
                System.exit(1);
            }

            NaiveBayes nb = new NaiveBayes (new File (argv[0]));
            for (int i = 1; i < argv.length; ++i) {
                nb.predict(argv[i]);
            }
        }
    }

    public static void main (String[] argv) throws Exception {
        logger.info
            ("## Please use either NaiveBayes$Train or NaiveBayes$Predict");
    }
}
