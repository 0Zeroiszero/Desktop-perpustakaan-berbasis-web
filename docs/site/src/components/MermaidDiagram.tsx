import { useEffect, useId, useState } from 'react';
import Box from '@mui/material/Box';
import CircularProgress from '@mui/material/CircularProgress';

export default function MermaidDiagram({ chart }: { chart: string }) {
  const rawId = useId();
  const [svg, setSvg] = useState('');
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    let mounted = true;
    const id = 'mermaid' + rawId.replace(/[^a-zA-Z0-9]/g, '');

    import('mermaid')
      .then(({ default: mermaid }) => {
        mermaid.initialize({
          startOnLoad: false,
          theme: 'base',
          securityLevel: 'strict',
          themeVariables: {
            primaryColor: '#EDE9FE',
            primaryTextColor: '#1E1B4B',
            primaryBorderColor: '#7C3AED',
            lineColor: '#6D28D9',
            fontSize: '14px',
            fontFamily: 'IBM Plex Sans, sans-serif',
          },
        });
        return mermaid.render(id, chart);
      })
      .then((result) => {
        if (mounted) {
          setSvg(result.svg);
        }
      })
      .catch(() => {
        if (mounted) {
          setFailed(true);
        }
      });

    return () => {
      mounted = false;
    };
  }, [chart, rawId]);

  if (failed) {
    return null;
  }

  if (!svg) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <CircularProgress size={24} aria-label="Memuat diagram" />
      </Box>
    );
  }

  return (
    <Box
      role="img"
      aria-label="Diagram alur aplikasi"
      sx={{
        p: 2,
        borderRadius: 2,
        bgcolor: 'background.paper',
        border: '1px solid',
        borderColor: 'divider',
        overflowX: 'auto',
        WebkitOverflowScrolling: 'touch',
        '& svg': { display: 'block', maxWidth: '100%', height: 'auto' },
      }}
      dangerouslySetInnerHTML={{ __html: svg }}
    />
  );
}
