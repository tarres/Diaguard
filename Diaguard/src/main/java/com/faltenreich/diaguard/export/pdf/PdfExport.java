package com.faltenreich.diaguard.export.pdf;

import android.os.AsyncTask;
import android.util.Log;

import com.faltenreich.diaguard.DiaguardApplication;
import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.export.Export;
import com.faltenreich.diaguard.export.ExportCallback;
import com.faltenreich.diaguard.export.ExportFormat;
import com.faltenreich.diaguard.export.pdf.print.PdfTable;
import com.faltenreich.diaguard.export.pdf.print.PdfWeekHeader;
import com.pdfjet.Point;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.io.File;

public class PdfExport extends AsyncTask<Void, String, File> {

    private static final String TAG = PdfExport.class.getSimpleName();

    private static final float PADDING_PARAGRAPH = 20;

    private PdfExportConfig config;

    public PdfExport(PdfExportConfig config) {
        this.config = config;
    }

    @Override
    protected File doInBackground(Void... params) {
        File file = Export.getExportFile(ExportFormat.PDF);
        try {
            PdfExportCache cache = new PdfExportCache(config, file);

            DateTime dateIteration = cache.getConfig().getDateStart();

            new PdfWeekHeader(cache, dateIteration).draw();

            // One day after last chosen day
            DateTime dateAfter = cache.getConfig().getDateEnd().plusDays(1);

            // Day by day
            while (dateIteration.isBefore(dateAfter)) {
                // title bar for new week
                if (dateIteration.isAfter(cache.getConfig().getDateStart()) && dateIteration.getDayOfWeek() == 1) {
                    cache.newPage();
                    new PdfWeekHeader(cache, dateIteration).draw();
                }

                PdfTable table = new PdfTable(cache, dateIteration);

                // Page break
                if ((cache.getCurrentPosition().getY() + table.getHeight() + PADDING_PARAGRAPH) > cache.getPage().getEndPoint().getY()) {
                    cache.newPage();
                    new PdfWeekHeader(cache, dateIteration).draw();
                }

                table.setPosition(cache.getCurrentPosition().getX(), cache.getCurrentPosition().getY());
                Point point = table.drawOn(cache.getPage());
                cache.setCurrentPosition(new Point(point.getX(), point.getY() + PADDING_PARAGRAPH));

                publishProgress(String.format("%s %d/%d",
                    DiaguardApplication.getContext().getString(R.string.day),
                    Days.daysBetween(cache.getConfig().getDateStart(), dateIteration).getDays() + 1,
                    Days.daysBetween(cache.getConfig().getDateStart(), cache.getConfig().getDateEnd()).getDays() + 1)
                );

                dateIteration = dateIteration.plusDays(1);
            }

            cache.getPdf().flush();
        } catch (Exception exception) {
            Log.e(TAG, exception.getMessage());
        }

        return file;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected void onProgressUpdate(String... message) {
        ExportCallback callback = config.getCallback();
        if (callback != null) {
            callback.onProgress(message[0]);
        }
    }

    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);
        ExportCallback callback = config.getCallback();
        if (callback != null) {
            if (file != null) {
                callback.onSuccess(file, PdfExportConfig.MIME_TYPE);
            } else {
                callback.onError();
            }
        }
    }
}
