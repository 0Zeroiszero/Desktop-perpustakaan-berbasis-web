import { useState } from 'react';
import Box from '@mui/material/Box';
import Chip from '@mui/material/Chip';
import IconButton from '@mui/material/IconButton';
import Link from '@mui/material/Link';
import Snackbar from '@mui/material/Snackbar';
import Stack from '@mui/material/Stack';
import Tooltip from '@mui/material/Tooltip';
import Typography from '@mui/material/Typography';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import Prism from 'prismjs';
import 'prismjs/components/prism-java';
import { repositoryBase, type CodeEntry } from '../data/content';

export default function CodeBlock({ entry }: { entry: CodeEntry }) {
  const [copied, setCopied] = useState(false);

  const copy = async () => {
    try {
      await navigator.clipboard.writeText(entry.snippet);
      setCopied(true);
    } catch {
      setCopied(false);
    }
  };

  const highlighted = Prism.highlight(entry.snippet, Prism.languages.java, 'java');

  return (
    <Box
      component="article"
      sx={{
        borderRadius: 2,
        border: '1px solid',
        borderColor: 'divider',
        overflow: 'hidden',
        bgcolor: 'background.paper',
      }}
    >
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        spacing={1}
        sx={{ px: 2, py: 1.5, bgcolor: '#F1F5F9', alignItems: { sm: 'center' }, justifyContent: 'space-between' }}
      >
        <Box sx={{ minWidth: 0 }}>
          <Link
            href={repositoryBase + entry.path}
            target="_blank"
            rel="noopener noreferrer"
            underline="hover"
            sx={{ fontFamily: '"JetBrains Mono", monospace', fontSize: 12.5, wordBreak: 'break-all' }}
          >
            {entry.path}
          </Link>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
            {entry.summary}
          </Typography>
        </Box>
        <Tooltip title="Salin kode">
          <IconButton
            aria-label="Salin kode"
            onClick={copy}
            size="small"
            sx={{ color: 'primary.main', flexShrink: 0 }}
          >
            <ContentCopyIcon fontSize="small" />
          </IconButton>
        </Tooltip>
      </Stack>

      <Chip
        label={entry.symbol}
        size="small"
        sx={{ m: 2, mb: 0, alignSelf: 'flex-start', bgcolor: '#EDE9FE', color: '#5B21B6', maxWidth: 'calc(100% - 32px)' }}
      />

      <Box
        component="pre"
        sx={{
          m: 2,
          mt: 1.5,
          p: 2,
          borderRadius: 1.5,
          bgcolor: '#0F172A',
          color: '#E2E8F0',
          fontFamily: '"JetBrains Mono", monospace',
          fontSize: { xs: 11.5, sm: 12.5 },
          lineHeight: 1.7,
          overflowX: 'auto',
          WebkitOverflowScrolling: 'touch',
        }}
      >
        <code className="language-java" dangerouslySetInnerHTML={{ __html: highlighted }} />
      </Box>

      <Snackbar
        open={copied}
        autoHideDuration={2000}
        onClose={() => setCopied(false)}
        message="Kode tersalin"
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      />
    </Box>
  );
}
