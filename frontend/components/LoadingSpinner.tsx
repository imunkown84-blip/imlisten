export default function LoadingSpinner({ label = 'Loading…' }: { label?: string }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-retro-muted gap-3 font-mono">
      <div className="w-8 h-8 border-3 border-retro-accent border-t-retro-ink rounded-full animate-spin border-2" />
      <span className="text-xs uppercase tracking-wider font-bold text-stone-600">{label}</span>
    </div>
  );
}

