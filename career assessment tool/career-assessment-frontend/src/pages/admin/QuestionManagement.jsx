import { useEffect, useState } from 'react';
import { adminService } from '../../services/adminService';
import PageHeader from '../../components/common/PageHeader';
import Table from '../../components/common/Table';
import Badge from '../../components/common/Badge';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import Input from '../../components/common/Input';
import { Select } from '../../components/common/Input';
import toast from 'react-hot-toast';
import { Plus, Pencil, Trash2, Search } from 'lucide-react';

const MOCK_QUESTIONS = [
  { id: 1, text: 'I enjoy being the center of attention.', type: 'PERSONALITY', category: 'Extraversion' },
  { id: 2, text: 'I prefer to stick to plans rather than improvise.', type: 'PERSONALITY', category: 'Conscientiousness' },
  { id: 3, text: 'How proficient are you in programming?', type: 'SKILLS', category: 'Technical' },
  { id: 4, text: 'Rate your data analysis skills.', type: 'SKILLS', category: 'Analytical' },
  { id: 5, text: 'I enjoy working with tools and machines.', type: 'INTEREST', category: 'Realistic' },
  { id: 6, text: 'I like investigating and solving puzzles.', type: 'INTEREST', category: 'Investigative' },
];

const EMPTY_FORM = { text: '', type: 'PERSONALITY', category: '', options: ['', '', '', '', ''] };

const QuestionManagement = () => {
  const [questions, setQuestions] = useState([]);
  const [filtered, setFiltered] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState('ALL');
  const [modal, setModal] = useState({ open: false, mode: 'add', data: null });
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const fetch = async () => {
      try {
        const { data } = await adminService.getAllQuestions('ALL');
        setQuestions(data);
      } catch {
        setQuestions(MOCK_QUESTIONS);
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, []);

  useEffect(() => {
    let result = questions;
    if (typeFilter !== 'ALL') result = result.filter((q) => q.type === typeFilter);
    if (search) result = result.filter((q) => q.text.toLowerCase().includes(search.toLowerCase()));
    setFiltered(result);
  }, [questions, typeFilter, search]);

  const openAdd = () => { setForm(EMPTY_FORM); setModal({ open: true, mode: 'add', data: null }); };
  const openEdit = (q) => { setForm({ text: q.text, type: q.type, category: q.category, options: q.options || ['', '', '', '', ''] }); setModal({ open: true, mode: 'edit', data: q }); };

  const handleSave = async (e) => {
    e.preventDefault();
    if (!form.text.trim()) { toast.error('Question text is required'); return; }
    setSaving(true);
    try {
      if (modal.mode === 'add') {
        await adminService.createQuestion(form);
        setQuestions((p) => [...p, { id: Date.now(), ...form }]);
        toast.success('Question added!');
      } else {
        await adminService.updateQuestion(modal.data.id, form);
        setQuestions((p) => p.map((q) => q.id === modal.data.id ? { ...q, ...form } : q));
        toast.success('Question updated!');
      }
      setModal({ open: false });
    } catch {
      toast.error('Failed to save question.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this question?')) return;
    try {
      await adminService.deleteQuestion(id);
      setQuestions((p) => p.filter((q) => q.id !== id));
      toast.success('Question deleted.');
    } catch {
      toast.error('Failed to delete.');
    }
  };

  const columns = [
    { key: 'text', label: 'Question', render: (v) => <span className="line-clamp-2 max-w-xs">{v}</span> },
    {
      key: 'type', label: 'Type',
      render: (v) => <Badge variant={v === 'PERSONALITY' ? 'primary' : v === 'SKILLS' ? 'info' : 'success'}>{v}</Badge>,
    },
    { key: 'category', label: 'Category' },
    {
      key: 'actions', label: 'Actions',
      render: (_, row) => (
        <div className="flex gap-2">
          <button onClick={() => openEdit(row)} className="p-1.5 text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors"><Pencil size={15} /></button>
          <button onClick={() => handleDelete(row.id)} className="p-1.5 text-red-500 hover:bg-red-50 rounded-lg transition-colors"><Trash2 size={15} /></button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <PageHeader title="Question Management" subtitle="Add, edit, and manage assessment questions.">
        <Button onClick={openAdd}><Plus size={16} /> Add Question</Button>
      </PageHeader>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3 mb-6">
        <div className="relative flex-1">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="Search questions..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-9 pr-4 py-2.5 rounded-xl border border-gray-300 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </div>
        <select
          value={typeFilter}
          onChange={(e) => setTypeFilter(e.target.value)}
          className="px-4 py-2.5 rounded-xl border border-gray-300 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
        >
          <option value="ALL">All Types</option>
          <option value="PERSONALITY">Personality</option>
          <option value="SKILLS">Skills</option>
          <option value="INTEREST">Interest</option>
        </select>
      </div>

      <Table columns={columns} data={filtered} loading={loading} emptyMessage="No questions found." />

      {/* Modal */}
      <Modal isOpen={modal.open} onClose={() => setModal({ open: false })} title={modal.mode === 'add' ? 'Add New Question' : 'Edit Question'} size="lg">
        <form onSubmit={handleSave} className="space-y-4">
          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium text-gray-700">Question Text *</label>
            <textarea
              rows={3}
              value={form.text}
              onChange={(e) => setForm({ ...form, text: e.target.value })}
              className="w-full px-4 py-2.5 rounded-xl border border-gray-300 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none"
              placeholder="Enter question text..."
            />
          </div>

          <div className="grid sm:grid-cols-2 gap-4">
            <Select label="Assessment Type" id="type" value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
              <option value="PERSONALITY">Personality</option>
              <option value="SKILLS">Skills</option>
              <option value="INTEREST">Interest</option>
            </Select>
            <Input label="Category" id="category" value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })} placeholder="e.g. Extraversion" />
          </div>

          <div>
            <label className="text-sm font-medium text-gray-700 block mb-2">Answer Options</label>
            <div className="space-y-2">
              {form.options.map((opt, i) => (
                <input
                  key={i}
                  type="text"
                  value={opt}
                  onChange={(e) => {
                    const opts = [...form.options];
                    opts[i] = e.target.value;
                    setForm({ ...form, options: opts });
                  }}
                  className="w-full px-4 py-2 rounded-xl border border-gray-300 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  placeholder={`Option ${i + 1}`}
                />
              ))}
            </div>
          </div>

          <div className="flex gap-3 pt-2">
            <Button type="submit" loading={saving}>{modal.mode === 'add' ? 'Add Question' : 'Save Changes'}</Button>
            <Button type="button" variant="secondary" onClick={() => setModal({ open: false })}>Cancel</Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default QuestionManagement;
